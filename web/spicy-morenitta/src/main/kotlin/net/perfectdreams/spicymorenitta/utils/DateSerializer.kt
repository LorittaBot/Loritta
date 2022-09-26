package net.perfectdreams.spicymorenitta.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.Date

class DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DataSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date {
        val date = Date(decoder.decodeString())
        return date
    }

    override fun serialize(encoder: Encoder, value: Date) {
        TODO("Not yet implemented")
    }
}