package top.sacz.timtool.hook.item.chat

import top.sacz.timtool.hook.base.BaseSwitchFunctionHookItem
import top.sacz.timtool.hook.core.annotation.HookItem
import top.sacz.xphelper.reflect.MethodUtils

@HookItem("辅助功能/聊天/正常打开卡片信息")
class SharedCardClickable : BaseSwitchFunctionHookItem() {

    override fun getTip(): String {
        return "去除点击卡片/链接时版本过低的提示，并能够正常查看"
    }

    override fun loadHook(classLoader: ClassLoader) {
        try {
            val targetMethod = MethodUtils.create("com.tencent.mobileqq.aio.msglist.holder.component.ark.d")
                .methodName("a")
                .params(String::class.java, String::class.java)
                .returnType(Boolean::class.javaPrimitiveType)
                .first()

            if (targetMethod != null) {
                hookAfter(targetMethod) { param ->
                    param.result = true
                }
            }
        } catch (e: Exception) {
        }
    }
}
