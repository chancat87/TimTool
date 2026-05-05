package top.sacz.timtool.app.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import top.sacz.timtool.net.UpdateService
import top.sacz.timtool.ui.components.MainScreen

class MainActivity : AppCompatActivity() {

    private val hideActivityName: String
        get() = "$packageName.app.activity.HideMainActivity"

    private val showActivityName: String
        get() = javaClass.name

    private val prefs by lazy { getSharedPreferences("timtool_prefs", Context.MODE_PRIVATE) }

    var isHideActivity: Boolean
        get() {
            return prefs.getBoolean("hide_icon", false)
        }
        set(enabled) {
            val hide = ComponentName(this, hideActivityName)
            hide.setEnable(this, enabled)
            val show = ComponentName(this, showActivityName)
            show.setEnable(this, !enabled)
            prefs.edit().putBoolean("hide_icon", enabled).apply()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initUpdateCheck()
        setContent {
            MainScreen(
                isHideIcon = isHideActivity,
                onToggleHideIcon = { enabled ->
                    isHideActivity = enabled
                }
            )
        }
    }

    private fun initUpdateCheck() {
        val updateService = UpdateService()
        updateService.requestUpdateAsync { hasUpdate ->
            if (hasUpdate) {
                updateService.showUpdateDialog()
            }
        }
    }
}

@SuppressLint("QueryPermissionsNeeded")
fun ComponentName.getEnable(ctx: Context): Boolean {
    val packageManager: PackageManager = ctx.packageManager
    val list = packageManager.queryIntentActivities(
        Intent().setComponent(this), PackageManager.MATCH_DEFAULT_ONLY
    )
    return list.isNotEmpty()
}

fun ComponentName.setEnable(ctx: Context, enabled: Boolean) {
    val packageManager: PackageManager = ctx.packageManager
    if (this.getEnable(ctx) == enabled) return
    packageManager.setComponentEnabledSetting(
        this,
        if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )
}
