package top.sacz.timtool.net.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Serializable
data class HasUpdate(
    @SerialName("hasUpdate") val hasUpdate: Boolean = false,
    @SerialName("isForceUpdate") val isForceUpdate: Boolean = false,
    @SerialName("version") val version: Int = 0
)

@Serializable
data class UpdateInfo(
    @SerialName("fileName") val fileName: String = "",
    @SerialName("forceUpdate") val forceUpdate: Boolean = false,
    @SerialName("id") val id: Int = 0,
    @SerialName("time") @Serializable(with = LocalDateTimeSerializer::class) val time: LocalDateTime = LocalDateTime.now(),
    @SerialName("updateLog") val updateLog: String = "",
    @SerialName("versionCode") val versionCode: Int = 0,
    @SerialName("versionName") val versionName: String = ""
)

internal object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    private val formatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
    )

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val value = decoder.decodeString()
        for (formatter in formatters) {
            try {
                return LocalDateTime.parse(value, formatter)
            } catch (_: DateTimeParseException) {
                continue
            }
        }
        return LocalDateTime.now()
    }
}
