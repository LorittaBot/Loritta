package net.perfectdreams.loritta.morenitta.messageverify

import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.messageverify.png.PNGChunk
import net.perfectdreams.loritta.morenitta.messageverify.png.PNGChunkUtils
import net.perfectdreams.loritta.morenitta.messageverify.savedmessage.SavedMessage
import net.perfectdreams.loritta.morenitta.messageverify.savedmessage.SavedUser
import net.perfectdreams.loritta.morenitta.utils.extensions.bytesToHex
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object LoriMessageDataUtils {
    const val CURRENT_VERSION = 1
    const val SUB_CHUNK_ID = "LORIMESSAGEDATA"

    // We do an allowlist approach instead of a denylist approach because it seems that Discord does somewhat manipulate PNG files (example: adding eXIf chunks at the end of files)
    // (they probably add the eXIf chunk as an attempt of scrubbing eXIf data)
    // So we will only hash some type of chunks, not all
    // Technically we could also hash other chunks, like PLTE, but we only care about the chunks that *actually* meaningfully changes the image's appearance
    // As in: We only care if an image can be substancially changed to the point that someone may believe that it wasn't tampered
    private val chunksToBeUsedForHashing = setOf(
        "IHDR", // Sets the image size
        "IDAT", // Sets the image data
        "acTL", // APNG animation control chunk
        "fcTL", // APNG frame control chunk
        "fdAT" // APNG frame data chunk
    )

    fun convertUserToSavedUser(user: User) = SavedUser(
        user.idLong,
        user.name,
        user.discriminator,
        user.globalName,
        user.avatarId,
        user.isBot,
        user.isSystem,
        user.flagsRaw
    )

    fun createSHA256HashOfImage(byteArray: ByteArray) = PNGChunkUtils.readChunksFromPNG(byteArray)

    fun createSHA256HashOfImage(chunks: List<PNGChunk>): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")

        for (chunk in chunks) {
            if (chunk.type in chunksToBeUsedForHashing) {
                md.update(chunk.data)
            }
        }

        return md.digest()
    }

    fun parseFromPNGChunk(loritta: LorittaBot, chunks: List<PNGChunk>, input: String): LoriMessageDataParseResult {
        val split = input.split(":")
        // error("Invalid input, split must be 4, not ${split.size}")
        if (split.size != 5)
            return LoriMessageDataParseResult.InvalidInput

        val id = split[0]
        // error("Not a Loritta message data")
        if (id != SUB_CHUNK_ID)
            return LoriMessageDataParseResult.InvalidInput

        val version = split[1].toIntOrNull()
        // error("Invalid version")
        if (version != CURRENT_VERSION)
            return LoriMessageDataParseResult.InvalidInput

        // Unused for now, but can be used in the future if the "chunksToBeUsedForHashing" list changes
        val hashChunkPack = split[2].toIntOrNull()
        if (hashChunkPack != 1)
            return LoriMessageDataParseResult.InvalidInput

        val data = split[3]
        val signature = split[4]

        // The signature is signed from the data AFTER it is encoded
        val imageSha256 = createSHA256HashOfImage(chunks).bytesToHex()

        // The signature is built like this:
        // "$LoriSecretKey:$HashedImage"
        // Where...
        // LoriSecretKey = A secret string that only Loritta knows
        // HashedImage = A hash of the image that includes the chunks provided in the "chunksToBeUsedForHashing" list
        val signingKey = SecretKeySpec("${loritta.config.loritta.messageVerification.encryptionKey}:$imageSha256".toByteArray(Charsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        val doneFinal = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        val output = doneFinal.bytesToHex()
        // error("Invalid signature!")
        if (output != signature)
            return LoriMessageDataParseResult.InvalidSignature

        // Yay, this is a CERTIFIED SIGNED VALID Loritta Message!
        val savedMessageAsJson = Base64.getDecoder().decode(data).toString(Charsets.UTF_8)

        return LoriMessageDataParseResult.Success(Json.decodeFromString(savedMessageAsJson))
    }

    sealed class LoriMessageDataParseResult {
        data object InvalidInput : LoriMessageDataParseResult()
        data object InvalidSignature : LoriMessageDataParseResult()
        data class Success(val savedMessage: SavedMessage) : LoriMessageDataParseResult()
    }
}