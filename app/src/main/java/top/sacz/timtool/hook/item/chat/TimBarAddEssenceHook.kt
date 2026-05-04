package top.sacz.timtool.hook.item.chat

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.children


import top.sacz.timtool.hook.item.api.QQContactUpdateListener
import top.sacz.timtool.hook.qqapi.QQEnvTool
import top.sacz.timtool.hook.util.ToastTool
import top.sacz.timtool.util.ScreenParamUtils
import top.sacz.xphelper.reflect.ClassUtils
import top.sacz.xphelper.reflect.FieldUtils
import top.sacz.xphelper.reflect.MethodUtils

import top.sacz.timtool.hook.base.BaseSwitchFunctionHookItem
import top.sacz.timtool.hook.core.annotation.HookItem

/**
 * 思路 QAuxiliary - me.hd.hook.TimBarAddEssenceHook
 */
@HookItem("辅助功能/聊天/TIM 群标题栏添加精华消息入口")
class TimBarAddEssenceHook : BaseSwitchFunctionHookItem() {

    private val layoutId = "TimBarAddEssenceHook".hashCode()

    override fun loadHook(classLoader: ClassLoader) {
        try {
            val timRight1VBClass = ClassUtils.findClass("com.tencent.tim.aio.titlebar.TimRight1VB")
            val redDotImageViewClass = ClassUtils.findClass("com.tencent.mobileqq.aio.widget.RedDotImageView")

            val targetMethod = MethodUtils.create(timRight1VBClass)
                .returnType(redDotImageViewClass)
                .first()

            hookAfter(targetMethod) { param ->
                val view = param.result as? View ?: return@hookAfter
                val rootView = view.parent as? ViewGroup ?: return@hookAfter

                if (rootView.children.any { it.id == layoutId }) {
                    return@hookAfter
                }

                val context = view.context
                val imageView = ImageView(context).apply {
                    layoutParams = RelativeLayout.LayoutParams(
                        ScreenParamUtils.dpToPx(context, 20f),
                        ScreenParamUtils.dpToPx(context, 20f)
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                        addRule(RelativeLayout.CENTER_VERTICAL)
                        marginEnd = ScreenParamUtils.dpToPx(context, 70f)
                    }
                    id = layoutId

                    try {
                        val iconResId = context.resources.getIdentifier(
                            "qui_tui_brand_products",
                            "drawable",
                            context.packageName
                        )
                        if (iconResId != 0) {
                            setImageResource(iconResId)
                        }
                    } catch (e: Exception) {
                    }

                    val nightMode = context.resources.configuration.uiMode and
                            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                    setColorFilter(if (nightMode) Color.WHITE else Color.BLACK)
                }

                imageView.setOnClickListener {
                    try {
                        val aioContact = QQContactUpdateListener.getCurrentAIOContact()

                        val chatType = FieldUtils.create(aioContact)
                            .fieldName("d")
                            .fieldType(Int::class.javaPrimitiveType)
                            .firstValue<Int>(aioContact)

                        if (chatType != 2) {
                            ToastTool.show("仅在群聊中可用")
                            return@setOnClickListener
                        }

                        var troopUin = FieldUtils.create(aioContact)
                            .fieldName("e")
                            .fieldType(String::class.java)
                            .firstValue<String>(aioContact)

                        if (!troopUin.matches(Regex("\\d+"))) {
                            try {
                                troopUin = QQEnvTool.getUinFromUid(troopUin)
                            } catch (e: Exception) {
                                ToastTool.show("无法获取群号")
                                return@setOnClickListener
                            }
                        }

                        val browserClass = ClassUtils.findClass("com.tencent.mobileqq.activity.QQBrowserDelegationActivity")
                        val intent = Intent(it.context, browserClass).apply {
                            putExtra("fling_action_key", 2)
                            putExtra("fling_code_key", it.context.hashCode())
                            putExtra("useDefBackText", true)
                            putExtra("param_force_internal_browser", true)
                            putExtra("url", "https://qun.qq.com/essence/index?gc=$troopUin")
                        }
                        it.context.startActivity(intent)
                    } catch (e: Exception) {
                        ToastTool.show("无法启动内置浏览器")
                        e.printStackTrace()
                    }
                }

                rootView.addView(imageView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
