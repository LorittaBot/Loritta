package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
class HashEncoder : AbstractEncoder() {
    val list = mutableListOf<Any>()

    override val serializersModule: SerializersModule = EmptySerializersModule

    // If this is not present, the serialization will fail due to "Optional" being null!
    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int) = false

    override fun encodeValue(value: Any) {
        list.add(value)
    }

    // Do nothing
    override fun encodeNull() {}
}