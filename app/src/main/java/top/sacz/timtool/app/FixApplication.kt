package top.sacz.timtool.app

import android.app.Application
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle
import top.sacz.xphelper.util.ConfigUtils

class FixApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ConfigUtils.initialize(this)
        DialogX.init(this)
        DialogX.globalTheme = DialogX.THEME.AUTO
        DialogX.globalStyle = MaterialYouStyle()
    }
}
