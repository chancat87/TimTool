package top.sacz.timtool.hook.item.chat

import top.sacz.timtool.hook.base.BaseSwitchFunctionHookItem
import top.sacz.timtool.hook.core.annotation.HookItem
import top.sacz.xphelper.reflect.MethodUtils

@HookItem("辅助功能/聊天/移除语音自动转文本灰字提示")
class RemoveSvipVoiceToTextGrayTip : BaseSwitchFunctionHookItem() {

    override fun getTip(): String {
        return "去你妈的自动语音转文本诱导开SBVIP消费 在tim4.0.98+测试通过"
    }

    override fun loadHook(classLoader: ClassLoader) {
        try {
        // Lcom/tencent/mobileqq/vas/perception/api/impl/VipPerceptionImpl;->addSVipLocalGrayTip(Ljava/lang/String;I)V
            val targetMethod = MethodUtils.create("com.tencent.mobileqq.vas.perception.api.impl.VipPerceptionImpl")
                .methodName("addSVipLocalGrayTip")
                .params(String::class.java, Int::class.javaPrimitiveType)
                .first()

            if (targetMethod != null) {
                hookBefore(targetMethod) { param ->
                    param.result = null
                }
            }
        } catch (e: Exception) {
        }
    }
}
