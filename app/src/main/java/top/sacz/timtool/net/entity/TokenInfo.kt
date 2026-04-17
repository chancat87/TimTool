package top.sacz.timtool.net.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenInfo(
    @SerialName("tokenName") val tokenName: String = "",
    @SerialName("tokenValue") val tokenValue: String = "",
    @SerialName("isLogin") val isLogin: Boolean = false,
    @SerialName("loginId") val loginId: kotlinx.serialization.json.JsonElement = kotlinx.serialization.json.JsonNull,
    @SerialName("loginType") val loginType: String = "",
    @SerialName("tokenTimeout") val tokenTimeout: Long = 0L,
    @SerialName("sessionTimeout") val sessionTimeout: Long = 0L,
    @SerialName("tokenSessionTimeout") val tokenSessionTimeout: Long = 0L,
    @SerialName("tokenActiveTimeout") val tokenActiveTimeout: Long = 0L,
    @SerialName("loginDevice") val loginDevice: String = "",
    @SerialName("tag") val tag: String = ""
)
