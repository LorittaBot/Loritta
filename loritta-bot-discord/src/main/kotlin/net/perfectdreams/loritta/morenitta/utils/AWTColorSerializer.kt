package net.perfectdreams.loritta.morenitta.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.awt.Color

/**
 * Serializes a [Color] into a packed RGB representation of the color
 */
object AWTColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeInt(value.rgb)
    }

    override fun deserialize(decoder: Decoder): Color {
        val rgb = decoder.decodeInt()
        return Color(rgb)
    }
}