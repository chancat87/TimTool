package top.artmoe.inao.item

import de.robv.android.xposed.XC_MethodHook
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import top.artmoe.inao.entries.InfoSyncPush
import top.artmoe.inao.entries.MsgPush
import top.artmoe.inao.entries.QQMessage

/**
 * 防撤回核心解析
 * by 叶叶,suzhelan
 */
@OptIn(ExperimentalSerializationApi::class)
object NewPreventRetractingMessageCore {
    fun interface OnRetractingMessage {
        fun onRetractingMessage(isTroop: Boolean, peerId: String, msgSeq: Int)
    }

    fun handleInfoSyncPush(buffer: ByteArray, param: XC_MethodHook.MethodHookParam, callback: OnRetractingMessage) {
        val infoSyncPush = ProtoBuf.Default.decodeFromByteArray<InfoSyncPush>(buffer)
        if (infoSyncPush.syncRecallContent == null) {
            return
        }
        val recallMsgSeqList = mutableListOf<Pair<String, Int>>()
        //新代码 构建新的InfoSyncPush
        val newSyncInfoBody = infoSyncPush.syncRecallContent.syncInfoBody.map { syncInfoBody ->
            if (syncInfoBody.msgList.isEmpty()) {
                return
            }
            val newMsgList = syncInfoBody.msgList.filter { qqMessage ->
                //断言 messageBody不为空
                check(qqMessage.messageBody != null)
                val msgType = qqMessage.messageContentInfo.msgType
                val msgSubType = qqMessage.messageContentInfo.msgSubType
                val isRecall =
                    (msgType == 732 && msgSubType == 17) || (msgType == 528 && msgSubType == 138)
                //是私聊消息
                if (msgType == 528 && msgSubType == 138) {
                    val opInfo = qqMessage.messageBody.operationInfo
                    val c2cRecall =
                        ProtoBuf.Default.decodeFromByteArray<QQMessage.MessageBody.C2CRecallOperationInfo>(
                            opInfo
                        )
                    val msgSeq = c2cRecall.info.msgSeq
                    val senderUid = qqMessage.messageHead.senderUid
                    callback.onRetractingMessage(false, senderUid, msgSeq)
                } else if (msgType == 732 && msgSubType == 17) {
                    //群聊消息
                    val opInfo = qqMessage.messageBody.operationInfo
                    val groupRecall =
                        ProtoBuf.Default.decodeFromByteArray<QQMessage.MessageBody.GroupRecallOperationInfo>(
                            opInfo
                        )
                    //groupUin
                    val groupPeerId = groupRecall.peerId.toString()
                    //msg seq
                    val recallMsgSeq = groupRecall.info.msgInfo.msgSeq
                    recallMsgSeqList.add(groupPeerId to recallMsgSeq)
                    callback.onRetractingMessage(true, groupPeerId, recallMsgSeq)
                }
                !isRecall
            }
            syncInfoBody.copy(msgList = newMsgList)
        }
        if (recallMsgSeqList.isEmpty()) {
            return
        }
        val newInfoSyncPush = infoSyncPush.copy(
            syncRecallContent = infoSyncPush.syncRecallContent.copy(
                syncInfoBody = newSyncInfoBody
            )
        )
        param.args[1] = ProtoBuf.Default.encodeToByteArray(newInfoSyncPush)
    }


    fun handleMsgPush(buffer: ByteArray, param: XC_MethodHook.MethodHookParam, callback: OnRetractingMessage) {
        val msgPush = ProtoBuf.Default.decodeFromByteArray<MsgPush>(buffer)
        //检查messageBody是否为空
        if (msgPush.qqMessage.messageBody == null) {
            return
        }
        val msg = msgPush.qqMessage
        val msgType = msg.messageContentInfo.msgType
        val subType = msg.messageContentInfo.subSeq
        val operationInfoByteArray = msg.messageBody.operationInfo

        when (msgType) {
            528 -> if (subType == 138) onC2CRecallByMsgPush(operationInfoByteArray, msgPush, param, callback)
            732 -> if (subType == 17) onGroupRecallByMsgPush(operationInfoByteArray, msgPush, param, callback)
        }
    }

    private fun onGroupRecallByMsgPush(
        operationInfoByteArray: ByteArray, // 1.3.2
        msgPush: MsgPush,
        param: XC_MethodHook.MethodHookParam,
        callback: OnRetractingMessage
    ) {
        //断言 messageBody不为空
        check(msgPush.qqMessage.messageBody != null)
        val firstPart = operationInfoByteArray.copyOfRange(0, 7) // 1.3.2.5 and 1.3.2.0
        val secondPart = operationInfoByteArray.copyOfRange(7, operationInfoByteArray.size)

        val operationInfo =
            ProtoBuf.Default.decodeFromByteArray<QQMessage.MessageBody.GroupRecallOperationInfo>(secondPart)

        operationInfo.info.operatorUid// 操作者UID

        //groupUin
        val groupPeerId = operationInfo.peerId.toString()
        //msg seq
        val recallMsgSeq = operationInfo.info.msgInfo.msgSeq

//        if (operatorUid == QQEnvTool.getUidFromUin(QQEnvTool.getCurrentUin())) return // 操作者是自己,不处理

        val newOperationInfoByteArray = firstPart + ProtoBuf.Default.encodeToByteArray(
            operationInfo.copy(
                msgSeq = 1,
                info = operationInfo.info.copy(
                    msgInfo = operationInfo.info.msgInfo.copy(msgSeq = 1)
                )
            )
        )

        val newMsgPush = msgPush.copy(
            qqMessage = msgPush.qqMessage.copy(
                messageBody = msgPush.qqMessage.messageBody.copy(
                    operationInfo = newOperationInfoByteArray
                )
            )
        )
        param.args[1] = ProtoBuf.Default.encodeToByteArray(newMsgPush)
        //回调给调用方
        callback.onRetractingMessage(true, groupPeerId, recallMsgSeq)
    }

    private fun onC2CRecallByMsgPush(
        operationInfoByteArray: ByteArray,
        msgPush: MsgPush,
        param: XC_MethodHook.MethodHookParam,
        callback: OnRetractingMessage
    ) {
        //断言 messageBody不为空
        check(msgPush.qqMessage.messageBody != null)
        val operationInfo =
            ProtoBuf.Default.decodeFromByteArray<QQMessage.MessageBody.C2CRecallOperationInfo>(
                operationInfoByteArray
            )

        //msg seq
        val recallMsgSeq = operationInfo.info.msgSeq
        //peerUid
        val operatorUid = operationInfo.info.operatorUid

        val newOperationInfoByteArray = ProtoBuf.Default.encodeToByteArray(
            operationInfo.copy(
                info = operationInfo.info.copy(msgSeq = 1)
            )
        )

        val newMsgPush = msgPush.copy(
            qqMessage = msgPush.qqMessage.copy(
                messageBody = msgPush.qqMessage.messageBody.copy(
                    operationInfo = newOperationInfoByteArray
                )
            )
        )

        param.args[1] = ProtoBuf.Default.encodeToByteArray(newMsgPush)
        //回调给调用方
        callback.onRetractingMessage(false, operatorUid, recallMsgSeq)
    }
}
