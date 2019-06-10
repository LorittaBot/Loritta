package net.perfectdreams.spicymorenitta.utils

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlin.js.Date

class TestSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName("WithCustomDefault")

    override fun serialize(output: Encoder, obj: Date) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deserialize(input: Decoder): Date {
        val date = Date(input.decodeString())
        return date
    }
}