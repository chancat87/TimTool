package top.sacz.timtool.hook.item.chat

import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout

import top.sacz.xphelper.reflect.ClassUtils

import top.sacz.timtool.hook.base.BaseSwitchFunctionHookItem
import top.sacz.timtool.hook.core.annotation.HookItem

/**
 * 思路 https://github.com/callng/TCQT/blob/main/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Fowo233%2Ftcqt%2Fhooks%2Ffunc%2Factivity%2FRemoveMenuIcon.kt#L1-L57
 */

@HookItem("辅助功能/聊天/移除长按菜单图标")
class RemoveMenuIcon : BaseSwitchFunctionHookItem() {

    override fun getTip(): String {
        return "移除长按消息气泡菜单中的图标，并自适应压缩菜单高度"
    }

    override fun loadHook(classLoader: ClassLoader) {
        val menuLayoutClass = ClassUtils.findClass("com.tencent.qqnt.aio.menu.ui.QQCustomMenuExpandableLayout")
            ?: return

        // 查找特征方法: private View o(int i, f fVar, boolean z, float[] fArr)
        // 无论混淆成 o 还是 l 还是 w，特征都是：返回值为View，四个参数：Int, Object, Boolean, FloatArray
        val targetMethod = menuLayoutClass.declaredMethods.firstOrNull { method ->
            val paramTypes = method.parameterTypes
            method.returnType == View::class.java &&
                    paramTypes.size == 4 &&
                    paramTypes[0] == Int::class.javaPrimitiveType &&
                    paramTypes[2] == java.lang.Boolean.TYPE &&
                    paramTypes[3] == FloatArray::class.java
        } ?: return

        hookBefore(targetMethod) { param ->
            val viewObj = param.thisObject as? View ?: return@hookBefore
            val density = viewObj.resources.displayMetrics.density

            val height71px = (71f * density + 0.5f).toInt()
            val height76px = (76f * density + 0.5f).toInt()

            val intFields = viewObj.javaClass.declaredFields.filter { it.type == Int::class.javaPrimitiveType }
            for (field in intFields) {
                field.isAccessible = true
                val value = field.get(viewObj) as? Int ?: continue

                if (value == height71px || value == height76px) {
                    val scale = 1.5f
                    val newHeight = (value / scale).toInt()
                    field.set(viewObj, newHeight)
                    break
                }
            }
        }

        hookAfter(targetMethod) { param ->
            val root = param.result as? LinearLayout ?: return@hookAfter
            if (root.childCount > 0 && root.getChildAt(0) is ImageView) {
                root.removeViewAt(0)
                root.gravity = Gravity.CENTER
            }
        }
    }
}
