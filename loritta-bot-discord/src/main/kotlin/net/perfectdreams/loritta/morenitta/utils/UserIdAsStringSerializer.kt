package net.perfectdreams.loritta.morenitta.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.perfectdreams.loritta.serializable.UserId

/**
 * Serializer that encodes and decodes [UserId] as its string representation.
 *
 * Intended to be used for interoperability with external clients (mainly JavaScript ones),
 * where numbers can't be parsed correctly if they exceed
 * [`abs(2^53-1)`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER).
 *
 * Based on kotlinx.serialization's original [LongAsStringSerializer]
 */
object UserIdAsStringSerializer : KSerializer<UserId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UserIdAsStringSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UserId) {
        encoder.encodeString(value.value.toString())
    }

    override fun deserialize(decoder: Decoder): UserId {
        return UserId(decoder.decodeString().toULong())
    }
}
