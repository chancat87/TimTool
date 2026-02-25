package top.artmoe.inao.entries

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MsgPush(
    @ProtoNumber(1) val qqMessage: QQMessage
)