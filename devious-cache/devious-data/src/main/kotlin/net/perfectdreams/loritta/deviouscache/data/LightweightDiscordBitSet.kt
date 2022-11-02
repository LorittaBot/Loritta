package net.perfectdreams.loritta.deviouscache.data


import dev.kord.common.DiscordBitSet
import dev.kord.common.entity.Permissions
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

private const val SAFE_LENGTH = 19
private const val WIDTH = Byte.SIZE_BITS

@Suppress("FunctionName")
public fun EmptyLightweightBitSet(): LightweightDiscordBitSet = LightweightDiscordBitSet(0)

@Serializable(with = LightweightDiscordBitSetSerializer::class)
@JvmInline
public value class LightweightDiscordBitSet(internal val data: LongArray) {

    public val isEmpty: Boolean
        get() = data.all { it == 0L }

    public val value: String
        get() {
            val buffer = ByteBuffer.allocate(data.size * Long.SIZE_BYTES)
            buffer.asLongBuffer().put(data.reversedArray())
            return BigInteger(buffer.array()).toString()
        }

    public val size: Int
        get() = data.size * WIDTH

    public val binary: String
        get() = data.joinToString("") { it.toULong().toString(2) }.reversed().padEnd(8, '0')

    private fun getOrZero(i: Int) = data.getOrNull(i) ?: 0L

    public operator fun get(index: Int): Boolean {
        if (index !in 0 until size) return false
        val indexOfWidth = index / WIDTH
        val bitIndex = index % WIDTH
        return data[indexOfWidth] and (1L shl bitIndex) != 0L
    }

    public operator fun contains(other: LightweightDiscordBitSet): Boolean {
        if (other.size > size) return false
        for (i in other.data.indices) {
            if (data[i] and other.data[i] != other.data[i]) return false
        }
        return true
    }

    public fun remove(another: LightweightDiscordBitSet) {
        for (i in 0 until min(data.size, another.data.size)) {
            data[i] = data[i] xor (data[i] and another.data[i])
        }
    }

    override fun toString(): String {
        return "LightweightDiscordBitSet($binary)"
    }

}

public fun LightweightDiscordBitSet(vararg widths: Long): LightweightDiscordBitSet {
    return LightweightDiscordBitSet(widths)
}

public fun LightweightDiscordBitSet(value: String): LightweightDiscordBitSet {
    if (value.length <= SAFE_LENGTH) {// fast path
        return LightweightDiscordBitSet(longArrayOf(value.toULong().toLong()))
    }

    val bytes = BigInteger(value).toByteArray()

    val longSize = (bytes.size / Long.SIZE_BYTES) + 1
    val destination = LongArray(longSize)

    var longIndex = -1
    bytes.reversed().forEachIndexed { index, byte ->
        val offset = index % Long.SIZE_BYTES
        if (offset == 0) {
            longIndex += 1
        }

        destination[longIndex] =
            (destination[longIndex].toULong() or (byte.toUByte().toULong() shl offset * Byte.SIZE_BITS)).toLong()
    }

    return LightweightDiscordBitSet(destination)
}


public object LightweightDiscordBitSetSerializer : KSerializer<LightweightDiscordBitSet> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("DiscordBitSet", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LightweightDiscordBitSet {
        return LightweightDiscordBitSet(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: LightweightDiscordBitSet) {
        encoder.encodeString(value.value)
    }
}