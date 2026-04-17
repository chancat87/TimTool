package top.sacz.timtool.hook.item.chat

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import top.artmoe.inao.item.FriendChatMessageRecall
import top.artmoe.inao.item.GroupChatMessageRecall
import top.artmoe.inao.item.NewPreventRetractingMessageCore
import top.artmoe.inao.item.RetractingCallback
import top.sacz.timtool.R
import top.sacz.timtool.hook.base.BaseSwitchFunctionHookItem
import top.sacz.timtool.hook.core.annotation.HookItem
import top.sacz.timtool.hook.item.api.QQMessageViewListener
import top.sacz.timtool.hook.item.api.QQMsgViewAdapter
import top.sacz.xphelper.reflect.ClassUtils
import top.sacz.xphelper.reflect.ConstructorUtils
import top.sacz.xphelper.reflect.FieldUtils
import top.sacz.xphelper.reflect.MethodUtils
import top.sacz.xphelper.util.ConfigUtils

@HookItem("辅助功能/聊天/消息防撤回")
class PreventRetractingMessage : BaseSwitchFunctionHookItem() {

    private val viewId = 0x298382
    private val config = ConfigUtils("防撤回")

    //使用map来拼接key查，性能通常比list更快
    private val friendChatMap = mutableMapOf<String, FriendChatMessageRecall>()
    private val groupChatMap = mutableMapOf<String, GroupChatMessageRecall>()

    private val friendChatList = mutableListOf<FriendChatMessageRecall>()
    private val groupChatList = mutableListOf<GroupChatMessageRecall>()

    /**
     * 缓存到本地
     */
    private fun readLocalCache() {
        val friendCache = config.getList("friendCache", FriendChatMessageRecall::class.java)
        friendChatList.addAll(friendCache)
        val groupCache = config.getList("groupCache", GroupChatMessageRecall::class.java)
        groupChatList.addAll(groupCache)
        //然后生成缓存到map
        for (friend in friendChatList) {
            friendChatMap[friend.peerUid + friend.msgSeq.toString()] = friend
        }
        for (group in groupChatList) {
            groupChatMap[group.groupUin + group.msgSeq.toString()] = group
        }
    }

    private fun addToFriendChatList(data: FriendChatMessageRecall) {
        friendChatList.add(data)
        friendChatMap[data.peerUid + data.msgSeq.toString()] = data
        config.put("friendCache", friendChatList)
    }

    private fun addToGroupChatList(data: GroupChatMessageRecall) {
        groupChatList.add(data)
        groupChatMap[data.groupUin + data.msgSeq.toString()] = data
        config.put("groupCache", groupChatList)
    }

    override fun loadHook(loader: ClassLoader) {
        readLocalCache()

        val onMSFPushMethod = MethodUtils.create("com.tencent.qqnt.kernel.nativeinterface.IQQNTWrapperSession\$CppProxy")
            .params(
                String::class.java,
                ByteArray::class.java,
                ClassUtils.findClass("com.tencent.qqnt.kernel.nativeinterface.PushExtraInfo")
            )
            .methodName("onMsfPush")
            .first()
        NewPreventRetractingMessageCore.setOnRecallMessageDetected(object : RetractingCallback {
            override fun onFriendChatMessageRecall(data: FriendChatMessageRecall) {
                addToFriendChatList(data)
            }

            override fun onGroupChatMessageRecall(data: GroupChatMessageRecall) {
                addToGroupChatList(data)
            }
        })
        hookBefore(onMSFPushMethod) { param ->
            val cmd = param.args[0] as String
            val protoBuf = param.args[1] as ByteArray
            if (cmd == "trpc.msg.register_proxy.RegisterProxy.InfoSyncPush") {
                NewPreventRetractingMessageCore.handleInfoSyncPush(protoBuf, param)
            } else if (cmd == "trpc.msg.olpush.OlPushService.MsgPush") {
                NewPreventRetractingMessageCore.handleMsgPush(protoBuf, param)
            }
        }
        hookAIOMsgUpdate()
    }


    private fun hookAIOMsgUpdate() {
        QQMessageViewListener.addMessageViewUpdateListener(
            this,
            object : QQMessageViewListener.OnChatViewUpdateListener {
                override fun onViewUpdateAfter(msgItemView: View, msgRecord: Any) {
                    //约束布局
                    val rootView = msgItemView as ViewGroup

                    //防止有撤回 进群等消息类型
                    if (!QQMsgViewAdapter.hasContentMessage(rootView)) return

                    val peerUid: String = FieldUtils.create(msgRecord)
                        .fieldName("peerUid")
                        .fieldType(String::class.java)
                        .firstValue(msgRecord)

                    val msgSeq: Long = FieldUtils.create(msgRecord)
                        .fieldName("msgSeq")
                        .fieldType(Long::class.javaPrimitiveType).firstValue(msgRecord)

                    //防止错误添加提示没有删除
                    val recallPromptTextView = rootView.findViewById<View>(viewId)
                    if (recallPromptTextView != null) rootView.removeView(recallPromptTextView)
                    //这个msg是秒级的 不是毫秒
                    var msgTime: Long = FieldUtils.create(msgRecord).fieldName("msgTime")
                        .fieldType(Long::class.javaPrimitiveType).firstValue(msgRecord)
                    //变成毫秒级
                    msgTime *= 1000
                    //计算时间差 发送时间低于1秒不判断
                    if ((System.currentTimeMillis() - msgTime) < 1000) {
                        return
                    }
                    //如果有那就是已经撤回的消息
                    if (isRetractMessage(peerUid, msgSeq.toInt())) {
                        addViewToQQMessageView(rootView)
                    }
                }

            })
    }

    private fun addViewToQQMessageView(rootView: ViewGroup) {
        val context = rootView.context
        val parentLayoutId = rootView.id
        val contentId: Int = QQMsgViewAdapter.getContentViewId()
        //制定约束布局参数 用反射做 不然androidx引用的是模块的而不是QQ自身的
        val newLayoutParams: LayoutParams = ConstructorUtils.newInstance(
            ClassUtils.findClass("androidx.constraintlayout.widget.ConstraintLayout\$LayoutParams"),
            arrayOf<Class<*>?>(
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            ),
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ) as LayoutParams
        FieldUtils.create(newLayoutParams)
            .fieldName("startToStart")
            .setFirst(newLayoutParams, parentLayoutId)

        FieldUtils.create(newLayoutParams)
            .fieldName("endToEnd")
            .setFirst(newLayoutParams, parentLayoutId)

        FieldUtils.create(newLayoutParams)
            .fieldName("topToTop")
            .setFirst(newLayoutParams, contentId)

        val textView = TextView(context)
        textView.text = "该消息已撤回"
        textView.id = viewId
        textView.gravity = Gravity.CENTER
        textView.textSize = 20f
        textView.setTextColor(context.getColor(R.color.皇家蓝))
        textView.isClickable = false
        rootView.addView(textView, newLayoutParams)
    }

    /**
     * 是否撤回的消息
     */
    private fun isRetractMessage(peerUid: String, msgSeq: Int): Boolean {
        val key = peerUid + msgSeq.toString()
        return friendChatMap.containsKey(key) || groupChatMap.containsKey(key)
    }


}
