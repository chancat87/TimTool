package top.sacz.timtool.hook.item.chat


import top.sacz.xphelper.reflect.ClassUtils
import top.sacz.xphelper.reflect.MethodUtils
import java.lang.reflect.Field

import top.sacz.timtool.hook.base.BaseSwitchFunctionHookItem
import top.sacz.timtool.hook.core.annotation.HookItem

@HookItem("辅助功能/群聊/@列表重新排序")
class SortAtList : BaseSwitchFunctionHookItem() {

    private var memberInfoClass: Class<*>? = null

    override fun getTip(): String {
        return "对@列表进行重新排序"
    }

    override fun loadHook(classLoader: ClassLoader) {
        memberInfoClass = ClassUtils.findClass("com.tencent.qqnt.kernel.nativeinterface.MemberInfo")

        val submitListEventClass = ClassUtils.findClass("com.tencent.mobileqq.aio.input.at.common.SubmitListEvent")
        if (submitListEventClass != null) {
            val getItemListMethod = MethodUtils.create(submitListEventClass)
                .methodName("getItemList")
                .returnType(List::class.java)
                .first()

            if (getItemListMethod != null) {
                hookAfter(getItemListMethod) { param ->
                    val originalList = param.result as? List<*> ?: return@hookAfter

                    val sortedList = originalList.sortedBy { item ->
                        rank(item)
                    }

                    param.result = sortedList
                }
            }
        }
    }

    private fun rank(item: Any?): Int {
        if (item == null) return 99
        val targetClass = memberInfoClass ?: return 0

        try {
            val memberInfoField = item.javaClass.declaredFields.find { field ->
                field.type == targetClass
            } ?: return 0

            memberInfoField.isAccessible = true
            val memberInfo = memberInfoField.get(item) ?: return 0

            val isRobotField = targetClass.getDeclaredField("isRobot")
            isRobotField.isAccessible = true
            val isRobot = isRobotField.getBoolean(memberInfo)

            if (isRobot) return 3

            val roleField = targetClass.getDeclaredField("role")
            roleField.isAccessible = true
            val roleObj = roleField.get(memberInfo)
            val roleName = roleObj?.toString() ?: ""

            return when {
                roleName.contains("OWNER") -> 1
                roleName.contains("ADMIN") -> 2
                roleName.contains("MEMBER") -> 4
                else -> 5
            }
        } catch (e: Exception) {
            return 0
        }
    }
}
