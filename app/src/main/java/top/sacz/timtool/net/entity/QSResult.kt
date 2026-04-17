package top.sacz.timtool.net.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QSResult<T>(
    @SerialName("code") val code: Int = 0,
    @SerialName("msg") val msg: String = "",
    @SerialName("action") val action: Int = 0,
    @SerialName("data") val data: T
) {

    fun isSuccess(): Boolean {
        return code == 200
    }

    override fun toString(): String {
        return "QSResult(code=$code, msg=$msg, action=$action, data=$data)"
    }
}
