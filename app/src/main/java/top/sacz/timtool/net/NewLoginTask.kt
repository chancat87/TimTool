package top.sacz.timtool.net

import com.alibaba.fastjson2.JSONObject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import top.sacz.timtool.BuildConfig
import top.sacz.timtool.hook.TimVersion
import top.sacz.timtool.hook.qqapi.QQEnvTool
import top.sacz.timtool.hook.util.LogUtils
import top.sacz.timtool.net.entity.RequestLogin
import top.sacz.timtool.net.entity.User
import top.sacz.timtool.util.IpUtil
import top.sacz.timtool.util.SystemUtil


class NewLoginTask {


    /**
     * 检查当前用户是否和token一致
     */
    private fun checkUserUin(): Boolean {
        val uin = QQEnvTool.getCurrentUin()
        return uin == UserCenter.getUserInfo().uin
    }

    /**
     * 非阻塞获取用户信息并保存到本地
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun loginAndGetUserInfoAsync() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val userApi = HttpClient.userApi
                val isLoginResult = userApi.isLogin().execute().body()
                if (isLoginResult == null || !isLoginResult.isSuccess()) {
                    return@launch
                }
                if (!isLoginResult.data) {
                    val uin = QQEnvTool.getCurrentUin()
                    val loginInfo = userApi.doLogin(RequestLogin(uin)).execute().body()?.data ?: return@launch
                    UserCenter.setUpdateToken(loginInfo)
                }
                commitInfoAsync()
                val user = userApi.getUserInfo().execute().body()?.data ?: return@launch
                UserCenter.setUserInfo(user)
                if (!checkUserUin()) {
                    UserCenter.removeAll()
                    awaitLogin()
                }
            } catch (e: Exception) {
                LogUtils.addError("login", e)
            }
        }
    }

    fun awaitLogin(): User = runBlocking {
        try {
            val userApi = HttpClient.userApi
            val isLoginResult = userApi.isLogin().execute().body()
            if (isLoginResult != null && isLoginResult.isSuccess() && !isLoginResult.data) {
                val uin = QQEnvTool.getCurrentUin()
                val loginInfo = userApi.doLogin(RequestLogin(uin)).execute().body()?.data
                if (loginInfo != null) {
                    UserCenter.setUpdateToken(loginInfo)
                }
            }
            val user = userApi.refresh().execute().body()?.data
                ?: return@runBlocking UserCenter.getUserInfo()
            UserCenter.setUserInfo(user)

            if (!checkUserUin()) {
                UserCenter.removeAll()
                return@runBlocking awaitLogin()
            }
            user
        } catch (e: Exception) {
            LogUtils.addError("login", e)
            UserCenter.getUserInfo()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun commitInfoAsync() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val param = JSONObject()
                //qq
                param["nickname"] = QQEnvTool.getCurrentAccountNickName()
                param["hostApp"] = TimVersion.getAppName()
                param["version"] = TimVersion.getVersionName()
                param["moduleVersion"] = BuildConfig.VERSION_NAME
                //android
                param["systemModel"] = SystemUtil.systemModel
                param["systemVersion"] = SystemUtil.systemVersion
                param["deviceBrand"] = SystemUtil.deviceBrand
                param["sdk"] = android.os.Build.VERSION.SDK_INT
                param["city"] = IpUtil.getCity()
                val requestBody = param.toJSONString().toRequestBody("application/json".toMediaType())
                //进行登录
                val userApi = HttpClient.userApi
                userApi.commitLoginInfo(requestBody).execute()
            } catch (e: Exception) {
                LogUtils.addError("commitInfo", e)
            }
        }
    }


}
