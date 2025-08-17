package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord.utils

import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.dv8tion.jda.api.entities.sticker.Sticker
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.RateLimitedException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.CommandException
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo
import kotlin.reflect.jvm.jvmName

sealed class ExpressionsManager(val context: UnleashedContext) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Expressions
    }

    enum class ExpressionType {
        STICKER,
        EMOJI,
        SFX // for the future
    }

    abstract val type: ExpressionType

    val allowedImageTypes = setOf("png", "gif", "json", "jpeg", "jpg")

    fun requireGuild() {
        if (context.guildOrNull == null) context.fail(true) {
            styled(
                context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds),
                Emotes.Error
            )
        }
    }

    sealed class OperationResult<T> {
        data class Success<T>(val data: T) : OperationResult<T>()
        data class Failure<T>(val handled: Boolean, val message: String? = null) : OperationResult<T>()
    }

    protected suspend fun handleDiscordAPIErrors(e: ErrorResponseException): Boolean {
        when (e.errorResponse) {
            ErrorResponse.FILE_UPLOAD_MAX_SIZE_EXCEEDED, ErrorResponse.CANNOT_RESIZE_BELOW_MAXIMUM -> {
                context.reply(true) {
                    val messageKey = when (type) {
                        ExpressionType.STICKER -> I18N_PREFIX.Sticker.Add.FileUploadMaxSizeExceeded
                        ExpressionType.EMOJI -> I18N_PREFIX.Emoji.Add.FileUploadMaxSizeExceeded
                        else -> return@reply // no support for soundboard effects in loritta yet
                    }
                    styled(context.i18nContext.get(messageKey))
                }

                return true
            }

            ErrorResponse.INVALID_FILE_UPLOADED, ErrorResponse.INVALID_FORM_BODY -> {
                context.reply(true) {
                    val messageKey = when (type) {
                        ExpressionType.STICKER -> I18N_PREFIX.Sticker.Add.InvalidUrl
                        ExpressionType.EMOJI -> I18N_PREFIX.Emoji.Add.InvalidUrl
                        else -> return@reply
                    }
                    styled(context.i18nContext.get(messageKey), Emotes.Error)
                }

                return true
            }

            ErrorResponse.MAX_EMOJIS -> {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Emoji.Add.MaxStaticEmojisLimitReached),
                        Emotes.Error
                    )
                }
                return true
            }

            ErrorResponse.MAX_ANIMATED_EMOJIS -> {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Emoji.Add.MaxAnimatedEmojisLimitReached),
                        Emotes.Error
                    )
                }
                return true
            }

            ErrorResponse.MAX_STICKERS -> {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Sticker.Add.MaxStickersReached),
                        Emotes.Error
                    )
                }
                return true
            }

            else -> return false
        }
    }

    protected suspend fun handleGenericError(e: Exception) {
        e.printStackTrace()

        context.reply(true) {
            styled(
                context.i18nContext.get(
                    I18nKeysData.Commands.ErrorWhileExecutingCommand(
                        Emotes.LoriRage,
                        Emotes.LoriSob,
                        e.message ?: "Exception name: ${e::class.jvmName}"
                    )
                ),
                Emotes.Error
            )
        }
    }

    protected suspend inline fun <reified T> asSafe(block: suspend () -> T): OperationResult<T> {
        return try {
            OperationResult.Success(block())
        } catch (e: CommandException) {
            throw e // re-throw
        } catch (e: ErrorResponseException) {
            val handled = handleDiscordAPIErrors(e)

            if (!handled) {
                handleGenericError(e)
            }

            OperationResult.Failure(true, e.message)
        } catch (e: Exception) {
            handleGenericError(e)
            OperationResult.Failure(true, e.message)
        }
    }

    protected fun processImageFile(file: String): Pair<ByteArray, String>? {
        val image = LorittaUtils.downloadFile(context.loritta, file) ?: return null

        val imageInfo = SimpleImageInfo(image)
        val originalType = imageInfo.mimeType?.substringAfter("/") ?: return null

        if (originalType !in allowedImageTypes) return null

        return when (originalType) {
            "jpeg", "jpg" -> {
                val i = LorittaUtils.convertImage(image, "png", true) ?: return null

                i to "png"
            }

            else -> image to originalType
        }
    }

    class StickerFactory(context: UnleashedContext) : ExpressionsManager(context) {
        companion object {
            private val I18N_PREFIX = I18nKeysData.Commands.Command.Expressions.Sticker
        }

        override val type = ExpressionType.STICKER

        fun getStickerFromName(name: String): Sticker? {
            requireGuild()

            return context.guild.stickers.firstOrNull { it.name == name }
        }

        /**
         * Adds a Sticker to the guild.
         * @param name Name of the sticker
         * @param description Description of the sticker
         * @param file The image file resolvable
         * @param tags Tags of the sticker (for blind people)
         */
        suspend fun addSticker(
            name: String,
            description: String,
            file: String,
            tags: List<String>,
            alreadyAck: Boolean = false
        ): Boolean? {
            return when (val result = asSafe {
                requireGuild()

                if (name.length !in 2..30) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Add.OutOfBoundsName),
                            Emotes.Error
                        )
                    }

                    return@asSafe false
                }

                if (description.length !in 2..100) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Add.OutOfBoundsDescription),
                            Emotes.Error
                        )
                    }

                    return@asSafe false
                }

                if (!alreadyAck)
                    context.deferChannelMessage(false)

                val imageData = processImageFile(file) ?: return@asSafe false

                try {
                    context.guild.createSticker(
                        name,
                        description,
                        FileUpload.fromData(imageData.first, "sticker.${imageData.second}"),
                        tags
                    ).submit().await()

                    true
                } catch (_: RateLimitedException) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Add.RateLimitExceeded),
                            Emotes.Error
                        )
                    }

                    false
                }
            }) {
                is OperationResult.Success -> result.data
                is OperationResult.Failure -> null
            }
        }

        /**
         * Removes a sticker from the guild.
         * @param id The sticker id
         */
        suspend fun removeSticker(id: Long): Boolean? = when (val result = asSafe<Boolean> {
            requireGuild()

            val parsedId = StickerSnowflake.fromId(id)

            context.guild.deleteSticker(parsedId).submit(false).await()

            return true
        }) {
            is OperationResult.Success -> result.data
            is OperationResult.Failure -> null
        }
    }

    class EmojiFactory(context: UnleashedContext) : ExpressionsManager(context) {
        companion object {
            private val I18N_PREFIX = I18nKeysData.Commands.Command.Expressions.Emoji
        }

        override val type = ExpressionType.EMOJI

        fun getEmojiFromName(name: String): RichCustomEmoji? {
            requireGuild()

            return context.guild.getEmojisByName(name, true).firstOrNull()
        }

        suspend fun addEmojiFromExistingEmoji(resolvable: String) = when (val result = asSafe<MutableList<RichCustomEmoji>> {
            requireGuild()

            val emojisToBeAdded = LorittaUtils.retrieveEmojis(resolvable)

            if (emojisToBeAdded.isEmpty())
                error("Couldn't find any emojis to add!")

            val added = mutableListOf<RichCustomEmoji>()

            for (emojiToBeAdded in emojisToBeAdded) {
                val img = LorittaUtils.downloadFile(context.loritta, emojiToBeAdded.url, connectTimeout = 5000)
                    ?: error("Couldn't download emoji image file!")

                val addedEmoji = try {
                    context.guild.createEmoji(
                        emojiToBeAdded.name,
                        Icon.from(img)
                    ).submit(false).await()
                } catch (e: RateLimitedException) {
                    throw e // propagate
                }

                if (addedEmoji != null) added.add(addedEmoji)
            }

            return@asSafe added
        }) {
            is OperationResult.Success -> result.data
            is OperationResult.Failure -> null
        }

        suspend fun addNewEmoji(name: String, data: String?) = when (val result = asSafe<RichCustomEmoji?> {
            val parsedEmoji = LorittaUtils.retrieveEmoji(name)

            if (data == null) error("Invalid URL for emote $name")

            val img: ByteArray = if (parsedEmoji == null) {
                LorittaUtils.downloadFile(context.loritta, data, connectTimeout = 5000)
                    ?: error("Invalid URL for emote $name")
            } else {
                LorittaUtils.downloadFile(context.loritta, parsedEmoji.url, connectTimeout = 5000)
                    ?: error("Invalid URL for emote $name")
            }

            val addedEmoji = try {
                context.guild.createEmoji(
                    parsedEmoji?.name ?: name,
                    Icon.from(img)
                ).submit(false).await()
            } catch (e: RateLimitedException) {
                throw e // propagate
            }

            return addedEmoji
        }) {
            is OperationResult.Success -> result.data
            is OperationResult.Failure -> null
        }

        suspend fun removeEmojis(resolvable: String): Int? = when (val result = asSafe<Int> {
            val emojis = context.guild.emojis
            val selectedEmojis = resolvable.removeSurrounding(":").split(" ")
            val fetchedEmojis = emojis.filter { it.asMention in selectedEmojis }
            val removedEmojisSize = fetchedEmojis.size

            if (fetchedEmojis.isEmpty())
                error("Couldn't find any emojis to remove!")

            fetchedEmojis.forEach {
                if (it.guild == context.guild) {
                    it.delete().queue()
                }
            }

            return@asSafe removedEmojisSize
        }) {
            is OperationResult.Success -> result.data
            is OperationResult.Failure -> null
        }
    }
}