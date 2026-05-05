package top.sacz.timtool.hook.item.message

import top.sacz.timtool.hook.base.BaseSwitchFunctionHookItem
import top.sacz.timtool.hook.core.annotation.HookItem
import top.sacz.xphelper.reflect.ClassUtils

@HookItem("辅助功能/聊天/屏蔽链接信息卡片")
class BlockLinkInfoCard : BaseSwitchFunctionHookItem() {

    override fun loadHook(classLoader: ClassLoader) {
        try {
            val linkInfoClass = ClassUtils.findClass("com.tencent.qqnt.kernel.nativeinterface.LinkInfo")
            if (linkInfoClass != null) {
                val targetConstructor = linkInfoClass.declaredConstructors.firstOrNull {
                    it.parameterTypes.size == 5
                }

                if (targetConstructor != null) {
                    hookBefore(targetConstructor) { param ->
                        if (param.args.isNotEmpty() && param.args[0] != null) {
                            param.result = null
                        }
                    }
                }
            }
        } catch (e: Exception) {
        }
    }
}
