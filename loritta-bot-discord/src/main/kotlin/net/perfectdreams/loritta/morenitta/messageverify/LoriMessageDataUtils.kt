package net.perfectdreams.loritta.morenitta.messageverify

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.GroupChannel
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.messageverify.png.PNGChunk
import net.perfectdreams.loritta.morenitta.messageverify.png.PNGChunkUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.bytesToHex
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.time.measureTimedValue

object LoriMessageDataUtils {
    private val logger = KotlinLogging.logger {}
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

    /**
     * Renders a [savedMessage] into a image using Loritta's DiscordChatMessageRendererServer
     */
    private suspend fun renderSavedMessage(loritta: LorittaBot, savedMessage: SavedMessage): ByteArray {
        return measureTimedValue {
            val response = loritta.httpWithoutTimeout.post(loritta.config.loritta.messageRenderer.rendererUrl.removeSuffix("/") + "/generate-message") {
                setBody(Json.encodeToString(savedMessage))
            }

            if (response.status != HttpStatusCode.OK)
                error("Something went wrong while trying to render a message screenshot for ${savedMessage.id}! Status code: ${response.status}; Body: ${response.bodyAsText()}")

            response.readBytes()
        }.also { logger.info { "Took ${it.duration} to ask DiscordChatMessageRendererServer to generate a message screenshot for ${savedMessage.id}!" } }.value
    }

    suspend fun createSignedRenderedSavedMessage(loritta: LorittaBot, savedMessage: SavedMessage): ByteArray {
        val screenshot = renderSavedMessage(loritta, savedMessage)
        val screenshotPNGChunks = PNGChunkUtils.readChunksFromPNG(screenshot)

        val b64Encoded = Base64.getEncoder().encodeToString(Json.encodeToString(savedMessage).toByteArray(Charsets.UTF_8))

        val hashOfTheImage = LoriMessageDataUtils.createSHA256HashOfImage(screenshotPNGChunks).bytesToHex()
        val signingKey = SecretKeySpec("${loritta.config.loritta.messageVerification.encryptionKey}:$hashOfTheImage".toByteArray(Charsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        val doneFinal = mac.doFinal(b64Encoded.toByteArray(Charsets.UTF_8))
        val output = doneFinal.bytesToHex()

        val finalImage = PNGChunkUtils.addChunkToPNG(screenshot, PNGChunkUtils.createTextPNGChunk("${LoriMessageDataUtils.SUB_CHUNK_ID}:${LoriMessageDataUtils.CURRENT_VERSION}:1:${b64Encoded}:$output"))

        return finalImage
    }

    fun createFileNameForSavedMessageImage(message: SavedMessage): String {
        // This makes the file name be a bit long, but it is useful to quickly check which user/guild/channel/message we are talking about
        val placeContext = message.placeContext
        return buildString {
            append("message-")
            append(message.author.id)
            if (placeContext is SavedGuild) {
                append("-")
                append(placeContext.id)
            }
            append("-")
            when (placeContext) {
                is SavedGroupChannel -> append(placeContext.id)
                is SavedGuild -> append(placeContext.channelId)
                is SavedPrivateChannel -> append(placeContext.id)
            }
            append("-")
            append(message.id)
            append(".lxrimsg.png")
        }
    }

    fun convertMessageToSavedMessage(message: Message): SavedMessage {
        return SavedMessage(
            message.idLong,
            if (message.hasGuild())
                SavedAttachedGuild(message.guild.idLong, message.channelIdLong, message.channel.name, message.channelType, message.guild.name, message.guild.iconId)
            else if (message.channelType == ChannelType.GROUP) {
                val channel = message.channel as GroupChannel

                SavedGroupChannel(
                    channel.idLong,
                    channel.name,
                    channel.iconId,
                )
            } else if (message.channelType == ChannelType.PRIVATE) {
                val channel = message.channel as PrivateChannel
                SavedPrivateChannel(channel.idLong)
            } else if (message.isFromGuild) {
                // If the message is from a guild, but we don't know it, then it must be a user app being used in a guild
                SavedDetachedGuild(message.guildIdLong, message.channelIdLong, message.channel.name, message.channelType)
            } else error("Unsupported channel type"),
            LoriMessageDataUtils.convertUserToSavedUser(message.author),
            message.member?.let {
                SavedMember(
                    it.nickname,
                    it.roles.map {
                        SavedRole(
                            it.idLong,
                            it.name,
                            it.colorRaw,
                            it.icon?.let {
                                val emoji = it.emoji
                                val iconId = it.iconId
                                if (emoji != null)
                                    SavedUnicodeRoleIcon(emoji)
                                else if (iconId != null)
                                    SavedCustomRoleIcon(iconId)
                                else
                                    null
                            }
                        )
                    }
                )
            },
            message.timeEdited?.toInstant()?.toKotlinInstant(),
            message.contentRaw,
            message.embeds.map {
                SavedEmbed(
                    it.type,
                    it.title,
                    it.description,
                    it.url,
                    if (it.colorRaw != Role.DEFAULT_COLOR_RAW) it.colorRaw else null,
                    it.author?.let {
                        SavedEmbed.SavedAuthor(
                            it.name,
                            it.url,
                            it.iconUrl,
                            it.proxyIconUrl
                        )
                    },
                    it.fields.map {
                        SavedEmbed.SavedField(
                            it.name,
                            it.value,
                            it.isInline
                        )
                    },
                    it.footer?.let {
                        SavedEmbed.SavedFooter(
                            it.text,
                            it.iconUrl,
                            it.proxyIconUrl
                        )
                    },
                    it.image?.let {
                        SavedEmbed.SavedImage(
                            it.url,
                            it.proxyUrl,
                            it.width,
                            it.height
                        )
                    },
                    it.thumbnail?.let {
                        SavedEmbed.SavedThumbnail(
                            it.url,
                            it.proxyUrl,
                            it.width,
                            it.height
                        )
                    }
                )
            },
            message.attachments.map {
                SavedAttachment(
                    it.idLong,
                    it.fileName,
                    it.description,
                    it.contentType,
                    it.size,
                    it.url,
                    it.proxyUrl,
                    if (it.width != -1) it.width else null,
                    if (it.height != -1) it.height else null,
                    it.isEphemeral,
                    it.duration,
                    it.waveform?.let { Base64.getEncoder().encodeToString(it) }
                )
            },
            message.stickers.map {
                SavedSticker(
                    it.idLong,
                    it.formatType,
                    it.name
                )
            },
            SavedMentions(
                message.mentions.users.map {
                    LoriMessageDataUtils.convertUserToSavedUser(it)
                },
                message.mentions.roles.map {
                    SavedRole(
                        it.idLong,
                        it.name,
                        it.colorRaw,
                        it.icon?.let {
                            val emoji = it.emoji
                            val iconId = it.iconId
                            if (emoji != null)
                                SavedUnicodeRoleIcon(emoji)
                            else if (iconId != null)
                                SavedCustomRoleIcon(iconId)
                            else
                                null
                        }
                    )
                }
            ),
            message.reactions.map {
                val emoji = it.emoji
                val savedEmoji = when (emoji) {
                    is CustomEmoji -> {
                        SavedCustomPartialEmoji(
                            emoji.idLong,
                            emoji.name,
                            emoji.isAnimated
                        )
                    }

                    is UnicodeEmoji -> {
                        SavedUnicodePartialEmoji(
                            emoji.name
                        )
                    }

                    else -> error("I don't know how to handle emoji type $emoji")
                }

                SavedReaction(
                    it.getCount(MessageReaction.ReactionType.NORMAL),
                    it.getCount(MessageReaction.ReactionType.SUPER),
                    savedEmoji
                )
            }
        )
    }

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
        val savedMessageAsJsonString = Base64.getDecoder().decode(data).toString(Charsets.UTF_8)
        val savedMessageAsJson = replaceTypeFields(
            Json.parseToJsonElement(savedMessageAsJsonString),
            // This is a workaround because old messages are saved with the full package name as its type
            mapOf(
                "net.perfectdreams.loritta.morenitta.messageverify.savedmessage.SavedAttachedGuild" to "attached_guild",
                "net.perfectdreams.loritta.morenitta.messageverify.savedmessage.SavedDetachedGuild" to "detached_guild",
                "net.perfectdreams.loritta.morenitta.messageverify.savedmessage.SavedPrivateChannel" to "private_channel",
                "net.perfectdreams.loritta.morenitta.messageverify.savedmessage.SavedGroupChannel" to "group_channel",
                "net.perfectdreams.loritta.morenitta.messageverify.savedmessage.SavedCustomRoleIcon" to "custom_icon",
                "net.perfectdreams.loritta.morenitta.messageverify.savedmessage.SavedUnicodeRoleIcon" to "unicode_icon",
                "net.perfectdreams.loritta.morenitta.messageverify.savedmessage.SavedCustomPartialEmoji" to "custom_emoji",
                "net.perfectdreams.loritta.morenitta.messageverify.savedmessage.SavedUnicodePartialEmoji" to "unicode_emoji"
            )
        )

        return LoriMessageDataParseResult.Success(Json.decodeFromJsonElement(savedMessageAsJson))
    }

    private fun replaceTypeFields(
        jsonElement: JsonElement,
        remapValues: Map<String, String>
    ): JsonElement {
        return when (jsonElement) {
            is JsonObject -> {
                JsonObject(
                    jsonElement.mapValues { (key, value) ->
                        if (key == "type" && value is JsonPrimitive && value.isString) {
                            val newString = remapValues[value.content]
                            JsonPrimitive(newString ?: value.content)
                        } else {
                            replaceTypeFields(value, remapValues)
                        }
                    }
                )
            }
            is JsonArray -> {
                JsonArray(jsonElement.map { element ->
                    replaceTypeFields(element, remapValues)
                })
            }
            else -> jsonElement
        }
    }

    sealed class LoriMessageDataParseResult {
        data object InvalidInput : LoriMessageDataParseResult()
        data object InvalidSignature : LoriMessageDataParseResult()
        data class Success(val savedMessage: SavedMessage) : LoriMessageDataParseResult()
    }
}