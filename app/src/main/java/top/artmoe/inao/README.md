## QQ Proto Buf 防撤回

所有人：言子飘花(https://github.com/Suzhelan),QStory,Tim小助手    
数据提供与初开发者: 叶叶(https://github.com/leafmoes)  
这是我很重要的人开发的 任何地方使用请保留原本注释和包名(top/artmoe/inao)  
如果你有更好的写法请告诉我

## 📖 用途说明

本文档是一套用于QQ消息防撤回功能的Protocol Buffer协议定义文件集合，主要用于：

- 拦截和处理QQ的消息撤回操作
- 解析QQ网络协议中的消息推送数据
- 实现消息防撤回的核心逻辑支撑

## 🔧 工作原理

### 核心机制

通过Hook QQ客户端的消息处理流程，拦截以下关键网络包：

1. **MsgPush** - 单条消息推送包
2. **InfoSyncPush** - 消息同步推送包

### 防撤回实现逻辑

1. 监听网络层消息接收
2. 识别撤回操作消息（msgType=528/subType=138 或 msgType=732/subType=17）
3. 修改撤回消息的关键字段（将msgSeq修改为无效值）
4. 缓存被撤回的消息信息用于UI展示
5. 让撤回操作在客户端层面失效

### 实现教程

原本的google protobuf已经弃用，现在使用kotlinx-serialization-protobuf来实现

1. 添加依赖

```groovy
//plugins添加
alias(libs.plugins.kotlinx.serialization)
//具体依赖你想找到应该不难
implementation(libs.kotlinx.serialization.protobuf)
implementation("com.github.suzhelan:XpHelper:3.0")
```

2. 复制此项目的 [app/src/main/javatop/artmoe/inao/entries](app/src/main/javatop/artmoe/inao/entries)
   目录中的所有文件到你的项目 保留包名
3. 编写hook代码，拦截protobuf包

```kotlin
val onMSFPushMethod =
    MethodUtils.create($$"com.tencent.qqnt.kernel.nativeinterface.IQQNTWrapperSession$CppProxy")
        .params(
            String::class.java,
            ByteArray::class.java,
            ClassUtils.getClass("com.tencent.qqnt.kernel.nativeinterface.PushExtraInfo")
        )
        .methodName("onMsfPush")
        .first()
hookBefore(onMSFPushMethod) { param: MethodHookParam ->
    val cmd = param.args[0] as String
    val protoBuf = param.args[1] as ByteArray
    if (cmd == "trpc.msg.register_proxy.RegisterProxy.InfoSyncPush") {
        PreventRetractingMessageCore.handleInfoSyncPush(protoBuf, param)
    } else if (cmd == "trpc.msg.olpush.OlPushService.MsgPush") {
        PreventRetractingMessageCore.handleMsgPush(protoBuf, param)
    }
}
```

4. 核心处理与拦截代码 方法结尾控制回调和显示即可

```kotlin
@OptIn(ExperimentalSerializationApi::class)
object PreventRetractingMessageCore {

    fun handleInfoSyncPush(buffer: ByteArray, param: XC_MethodHook.MethodHookParam) {
        val infoSyncPush = ProtoBuf.decodeFromByteArray<InfoSyncPush>(buffer)
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
                        ProtoBuf.decodeFromByteArray<QQMessage.MessageBody.C2CRecallOperationInfo>(
                            opInfo
                        )
                    val msgSeq = c2cRecall.info.msgSeq
                    val senderUid = qqMessage.messageHead.senderUid
                    recallMsgSeqList.add(senderUid to msgSeq)
                } else if (msgType == 732 && msgSubType == 17) {
                    //群聊消息
                    val opInfo = qqMessage.messageBody.operationInfo
                    val groupRecall =
                        ProtoBuf.decodeFromByteArray<QQMessage.MessageBody.GroupRecallOperationInfo>(
                            opInfo
                        )
                    //groupUin
                    val groupPeerId = groupRecall.peerId.toString()
                    //msg seq
                    val recallMsgSeq = groupRecall.info.msgInfo.msgSeq
                    recallMsgSeqList.add(groupPeerId to recallMsgSeq)
                }
                !isRecall
            }
            syncInfoBody.copy(msgList = newMsgList)
        }
        val newInfoSyncPush = infoSyncPush.copy(
            syncRecallContent = infoSyncPush.syncRecallContent.copy(
                syncInfoBody = newSyncInfoBody
            )
        )
        param.args[1] = ProtoBuf.encodeToByteArray(InfoSyncPush.serializer(), newInfoSyncPush)
        val retracting =
            HookItemLoader.HookInstance[PreventRetractingMessage::class.java] as PreventRetractingMessage
        recallMsgSeqList.forEach { (peerId, msgSeq) ->
            retracting.writeAndRefresh(peerId, msgSeq)
        }
    }


    fun handleMsgPush(buffer: ByteArray, param: XC_MethodHook.MethodHookParam) {
        val msgPush = ProtoBuf.decodeFromByteArray<MsgPush>(buffer)
        //检查messageBody是否为空
        if (msgPush.qqMessage.messageBody == null) {
            return
        }
        val msg = msgPush.qqMessage
        val msgType = msg.messageContentInfo.msgType
        val subType = msg.messageContentInfo.subSeq
        val operationInfoByteArray = msg.messageBody.operationInfo

        when (msgType) {
            528 -> if (subType == 138) onC2CRecallByMsgPush(operationInfoByteArray, msgPush, param)
            732 -> if (subType == 17) onGroupRecallByMsgPush(operationInfoByteArray, msgPush, param)
        }
    }

    private fun onGroupRecallByMsgPush(
        operationInfoByteArray: ByteArray, // 1.3.2
        msgPush: MsgPush,
        param: XC_MethodHook.MethodHookParam
    ) {
        //断言 messageBody不为空
        check(msgPush.qqMessage.messageBody != null)
        val firstPart = operationInfoByteArray.copyOfRange(0, 7) // 1.3.2.5 and 1.3.2.0
        val secondPart = operationInfoByteArray.copyOfRange(7, operationInfoByteArray.size)

        val operationInfo =
            ProtoBuf.decodeFromByteArray<QQMessage.MessageBody.GroupRecallOperationInfo>(secondPart)

        val operatorUid = operationInfo.info.operatorUid// 操作者UID

        //groupUin
        val groupPeerId = operationInfo.peerId.toString()
        //msg seq
        val recallMsgSeq = operationInfo.info.msgInfo.msgSeq

        if (operatorUid == QQEnvTool.getUidFromUin(QQEnvTool.getCurrentUin())) return // 操作者是自己,不处理

        val newOperationInfoByteArray = firstPart + ProtoBuf.encodeToByteArray(
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
        param.args[1] = ProtoBuf.encodeToByteArray(newMsgPush)
        //写入撤回缓存 给ui显示
        val retracting =
            HookItemLoader.HookInstance[PreventRetractingMessage::class.java] as PreventRetractingMessage
        retracting.writeAndRefresh(groupPeerId, recallMsgSeq)

    }

    private fun onC2CRecallByMsgPush(
        operationInfoByteArray: ByteArray,
        msgPush: MsgPush,
        param: XC_MethodHook.MethodHookParam
    ) {
        //断言 messageBody不为空
        check(msgPush.qqMessage.messageBody != null)
        val operationInfo =
            ProtoBuf.decodeFromByteArray<QQMessage.MessageBody.C2CRecallOperationInfo>(
                operationInfoByteArray
            )

        //msg seq
        val recallMsgSeq = operationInfo.info.msgSeq
        //peerUid
        val operatorUid = operationInfo.info.operatorUid

        val newOperationInfoByteArray = ProtoBuf.encodeToByteArray(
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

        param.args[1] = ProtoBuf.encodeToByteArray(newMsgPush)
        //写入缓存
        val retracting =
            HookItemLoader.HookInstance[PreventRetractingMessage::class.java] as PreventRetractingMessage
        retracting.writeAndRefresh(operatorUid, recallMsgSeq)


    }
}
```