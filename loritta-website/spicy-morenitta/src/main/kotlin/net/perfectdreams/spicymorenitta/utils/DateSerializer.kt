package net.perfectdreams.spicymorenitta.utils

import kotlinx.serialization.*
import kotlin.js.Date

class DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("DataSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deserialize(decoder: Decoder): Date {
        val date = Date(decoder.decodeString())
        return date
    }
}