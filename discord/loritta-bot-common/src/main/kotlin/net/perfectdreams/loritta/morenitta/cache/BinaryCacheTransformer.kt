package net.perfectdreams.loritta.morenitta.cache

import com.github.luben.zstd.Zstd
import com.github.luben.zstd.ZstdDictCompress
import com.github.luben.zstd.ZstdDictDecompress
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import net.perfectdreams.loritta.cinnamon.discord.utils.entitycache.DiscordCacheService
import net.perfectdreams.loritta.cinnamon.discord.utils.entitycache.ZstdDictionaries
import java.nio.ByteBuffer

class BinaryCacheTransformer<T>(
    val zstdDictionaries: ZstdDictionaries,
    val dictionary: ZstdDictionaries.Dictionary
) {
    fun compressWithZstd(payload: String) = Zstd.compress(payload.toByteArray(Charsets.UTF_8), 2)
    fun compressWithZstd(payload: String, dictCompress: ZstdDictCompress) =
        Zstd.compress(payload.toByteArray(Charsets.UTF_8), dictCompress)

    fun decompressWithZstd(payload: ByteArray): ByteArray =
        Zstd.decompress(payload, Zstd.decompressedSize(payload).toInt())

    fun decompressWithZstd(payload: ByteArray, dictDecompress: ZstdDictDecompress): ByteArray =
        Zstd.decompress(payload, dictDecompress, Zstd.decompressedSize(payload).toInt())

    fun enumToDict(dictionary: ZstdDictionaries.Dictionary) = when (dictionary) {
        ZstdDictionaries.Dictionary.NO_DICTIONARY -> null
        ZstdDictionaries.Dictionary.ROLES_V1 -> zstdDictionaries.rolesV1
        ZstdDictionaries.Dictionary.CHANNELS_V1 -> zstdDictionaries.channelsV1
        ZstdDictionaries.Dictionary.EMOJIS_V1 -> zstdDictionaries.emojisV1
    }
}

/**
 * Encodes and compresses the [payload] to binary, useful to be stored in an in-memory database (such as Redis)
 */
inline fun <reified T> BinaryCacheTransformer<T>.encode(payload: T): ByteArray {
    val zstdCompress = enumToDict(dictionary)?.compress

    val compressedWithZstd = if (zstdCompress == null)
        compressWithZstd(Json.encodeToString<T>(payload))
    else
        compressWithZstd(Json.encodeToString<T>(payload), zstdCompress)

    val header = DiscordCacheService.LorittaCompressionHeader(0, dictionary)
    val headerAsByteArray = ProtoBuf.encodeToByteArray(header)

    val newArray = ByteArray(4 + headerAsByteArray.size + 4 + compressedWithZstd.size)
    val byteBuf = ByteBuffer.wrap(newArray)
    byteBuf.putInt(headerAsByteArray.size)
    byteBuf.put(headerAsByteArray)
    byteBuf.putInt(compressedWithZstd.size)
    byteBuf.put(compressedWithZstd)

    return newArray
}

/**
 * Decodes and decompresses the [payload] from binary, encoded with [encodeToBinary]
 */
inline fun <reified T> BinaryCacheTransformer<T>.decode(payload: ByteArray): T {
    val byteBuf = ByteBuffer.wrap(payload)

    // Loritta's Compression Header
    val headerLengthInBytes = byteBuf.int
    val headerBytes = ByteArray(headerLengthInBytes)
    byteBuf.get(headerBytes)

    val compressionHeader = ProtoBuf.decodeFromByteArray<DiscordCacheService.LorittaCompressionHeader>(headerBytes)

    if (compressionHeader.version != 0)
        error("Unknown compression version ${compressionHeader.version}!")

    val zstdDecompress = enumToDict(compressionHeader.dictionaryId)?.decompress

    val zstdPayloadLength = byteBuf.int
    val zstdPayload = ByteArray(zstdPayloadLength)
    byteBuf.get(zstdPayload)

    val decompressed = if (zstdDecompress == null)
        decompressWithZstd(zstdPayload)
    else
        decompressWithZstd(zstdPayload, zstdDecompress)

    val byteArrayAsString = decompressed.toString(Charsets.UTF_8)
    return Json.decodeFromString<T>(byteArrayAsString)
}