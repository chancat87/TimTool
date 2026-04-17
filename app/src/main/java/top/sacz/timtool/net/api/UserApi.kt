package top.sacz.timtool.net.api

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import top.sacz.timtool.net.entity.QSResult
import top.sacz.timtool.net.entity.RequestLogin
import top.sacz.timtool.net.entity.TokenInfo
import top.sacz.timtool.net.entity.User

/**
 * 在宿主环境 不能使用suspend搭配协程
 */
interface UserApi {

    @POST("/user/doLogin")
    @Headers("Content-Type: application/json")
    fun doLogin(@Body param: RequestLogin): Call<QSResult<TokenInfo>>

    @POST("/user/info")
    fun getUserInfo(): Call<QSResult<User>>

    @POST("/user/refreshUserInfo")
    fun refresh(): Call<QSResult<User>>

    @POST("/user/commitLoginInfo")
    @Headers("Content-Type: application/json")
    fun commitLoginInfo(@Body body: RequestBody): Call<QSResult<String>>

    @POST("/user/isLogin")
    fun isLogin(): Call<QSResult<Boolean>>

}
