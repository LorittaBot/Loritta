package net.perfectdreams.loritta.deviouscache.data

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = LightweightPermissions.Companion::class)
@JvmInline
public value class LightweightPermissions(val code: LightweightDiscordBitSet) {
    /**
     * Returns this [Permissions] as a [Set] of [Permission]s, not including any [unknown][Permission.Unknown]
     * permissions.
     */
    override fun toString(): String {
        return "Permissions(values=$code)"
    }

    public companion object : KSerializer<LightweightPermissions> {

        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("permission", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): LightweightPermissions {
            val permissions = decoder.decodeString()
            return LightweightPermissions(permissions)
        }

        override fun serialize(encoder: Encoder, value: LightweightPermissions) {
            val permissionsSet = value.code.value
            encoder.encodeString(permissionsSet)
        }

    }
}

public fun LightweightPermissions(value: Permissions): LightweightPermissions = LightweightPermissions(LightweightDiscordBitSet(value.code.value))
public fun LightweightPermissions(value: String): LightweightPermissions = LightweightPermissions(LightweightDiscordBitSet(value))
