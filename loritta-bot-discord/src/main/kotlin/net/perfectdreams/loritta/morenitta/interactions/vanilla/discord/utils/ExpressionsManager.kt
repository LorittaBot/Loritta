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
import net.perfectdreams.loritta.morenitta.interactions.vanilla.discord.ExpressionsCommand
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

    suspend inline fun <reified T> avoidCommonExceptions(block: suspend () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            when (e) {
                is ErrorResponseException -> {
                    when (e.errorResponse) {
                        ErrorResponse.FILE_UPLOAD_MAX_SIZE_EXCEEDED, ErrorResponse.CANNOT_RESIZE_BELOW_MAXIMUM -> {
                            context.reply(true) {
                                when (type) {
                                    ExpressionType.STICKER -> {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.Sticker.Add.FileUploadMaxSizeExceeded)
                                        )
                                    }

                                    ExpressionType.EMOJI -> {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.Emoji.Add.FileUploadMaxSizeExceeded)
                                        )
                                    }

                                    else -> {} // there's no support to add soundboard effects with Loritta yet
                                }
                            }

                            return null
                        }

                        ErrorResponse.INVALID_FILE_UPLOADED, ErrorResponse.INVALID_FORM_BODY -> {
                            context.reply(true) {
                                when (type) {
                                    ExpressionType.STICKER -> {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.Sticker.Add.InvalidUrl),
                                            Emotes.Error
                                        )
                                    }

                                    ExpressionType.EMOJI -> {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.Emoji.Add.InvalidUrl),
                                            Emotes.Error
                                        )
                                    }

                                    else -> {} // nothing yet.
                                }
                            }

                            return null
                        }

                        ErrorResponse.MAX_EMOJIS -> {
                            context.reply(true) {
                                styled(
                                    context.i18nContext.get(
                                        I18N_PREFIX.Emoji.Add.MaxStaticEmojisLimitReached,
                                    ),
                                    Emotes.Error
                                )
                            }

                            return null
                        }

                        ErrorResponse.MAX_ANIMATED_EMOJIS -> {
                            context.reply(true) {
                                styled(
                                    context.i18nContext.get(
                                        ExpressionsCommand.Companion.I18N_PREFIX.Emoji.Add.MaxAnimatedEmojisLimitReached
                                    ),
                                    Emotes.Error
                                )
                            }

                            return null
                        }

                        ErrorResponse.MAX_STICKERS -> {
                            context.reply(true) {
                                styled(
                                    context.i18nContext.get(I18N_PREFIX.Sticker.Add.MaxStickersReached),
                                    Emotes.Error
                                )
                            }

                            return null
                        }

                        else -> {
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

                            return null
                        }
                    }
                }

                is CommandException -> throw e

                else -> {
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

                    return null
                }
            }
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
        ): Boolean? = avoidCommonExceptions {
            requireGuild()

            if (name.length < 2 || name.length > 30) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Add.OutOfBoundsName),
                        Emotes.Error
                    )
                }

                return@avoidCommonExceptions false
            }

            if (description.length < 2 || description.length > 100) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Add.OutOfBoundsDescription),
                        Emotes.Error
                    )
                }

                return@avoidCommonExceptions false
            }

            if (!alreadyAck)
                context.deferChannelMessage(false)

            val image = LorittaUtils.downloadFile(context.loritta, file) ?: run {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Add.InvalidUrl),
                        Emotes.Error
                    )
                }

                return@avoidCommonExceptions false
            }

            var imageInfo = SimpleImageInfo(image)
            var imageType = imageInfo.mimeType!!.split("/")[1]

            val imageData = if (imageType in allowedImageTypes) {
                when (imageType) {
                    "jpeg", "jpg" -> LorittaUtils.convertImage(image, "png", true)
                    else -> image
                }
            } else {
                null
            } ?: run {
                context.reply(true) {
                    styled(
                        "Something went wrong while converting image from $imageType to png... oopsie!",
                        Emotes.Error
                    )
                }

                return@avoidCommonExceptions false
            }

            imageInfo = SimpleImageInfo(imageData)
            imageType = imageInfo.mimeType!!.split("/")[1]

            return@avoidCommonExceptions try {
                context.guild.createSticker(
                    name,
                    description,
                    FileUpload.fromData(imageData, "sticker.$imageType"),
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
        }

        /**
         * Removes a sticker from the guild.
         * @param id The sticker id
         */
        suspend fun removeSticker(id: Long) {
            requireGuild()

            val parsedId = StickerSnowflake.fromId(id)

            context.guild.deleteSticker(parsedId).submit(false).await()

            context.reply(false) {
                styled(
                    context.i18nContext.get(ExpressionsCommand.Companion.I18N_PREFIX.Sticker.Remove.SuccessfullyRemovedStickerMessage),
                    Emotes.LoriHappyJumping
                )
            }
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

        suspend fun addEmojiFromExistingEmoji(resolvable: String) = avoidCommonExceptions {
            requireGuild()

            val emojisToBeAdded = LorittaUtils.retrieveEmojis(resolvable)

            if (emojisToBeAdded.isEmpty()) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Add.CouldntFindAnyEmojis),
                        Emotes.Error
                    )
                }

                return@avoidCommonExceptions
            }

            context.deferChannelMessage(false)

            val added = mutableListOf<RichCustomEmoji>()

            for (emojiToBeAdded in emojisToBeAdded) {
                val img = LorittaUtils.downloadFile(context.loritta, emojiToBeAdded.url, connectTimeout = 5000)
                    ?: run {
                        context.reply(true) {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.Add.InvalidUrl),
                                Emotes.Error
                            )
                        }

                        return@avoidCommonExceptions
                    }

                val addedEmoji = try {
                    context.guild.createEmoji(
                        emojiToBeAdded.name,
                        Icon.from(img)
                    ).submit(false).await()
                } catch (e: RateLimitedException) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Add.RateLimitExceeded),
                            Emotes.Error
                        )
                    }

                    null
                }

                if (addedEmoji != null) added.add(addedEmoji)
            }

            context.reply(false) {
                if (added.size > 1) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Add.SuccessfullyBulkAdded(added.joinToString(", ") { it.asMention })),
                        Emotes.LoriHappyJumping
                    )
                } else {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Add.SuccessfullyAdded("(${added.joinToString(" ") { it.asMention }})")),
                        Emotes.LoriHappyJumping
                    )
                }
            }
        }

        suspend fun addNewEmoji(name: String, data: String?) = avoidCommonExceptions {
            context.deferChannelMessage(false)

            val parsedEmoji = LorittaUtils.retrieveEmoji(name)

            lateinit var img: ByteArray

            if (data == null) context.fail(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.Add.InvalidUrl),
                    Emotes.Error
                )
            }

            if (parsedEmoji == null) {
                img = LorittaUtils.downloadFile(context.loritta, data, connectTimeout = 5000)
                    ?: context.fail(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Add.InvalidUrl),
                            Emotes.Error
                        )
                    }
            } else {
                LorittaUtils.downloadFile(context.loritta, parsedEmoji.url, connectTimeout = 5000) ?: context.fail(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Add.InvalidUrl),
                        Emotes.Error
                    )
                }
            }

            val addedEmoji = try {
                context.guild.createEmoji(
                    parsedEmoji?.name ?: name,
                    Icon.from(img)
                ).submit(false).await()
            } catch (e: RateLimitedException) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Add.RateLimitExceeded),
                        Emotes.Error
                    )
                }
            }

            context.reply(false) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.Add.SuccessfullyAdded(addedEmoji.asMention)),
                    Emotes.LoriHappyJumping
                )
            }
        }

        suspend fun removeEmojis(resolvable: String) = avoidCommonExceptions {
            context.deferChannelMessage(false)

            val emojis = context.guild.emojis
            val selectedEmojis = resolvable.removeSurrounding(":").split(" ")
            val fetchedEmojis = emojis.filter { it.asMention in selectedEmojis }
            val removedEmojisSize = fetchedEmojis.size

            if (fetchedEmojis.isEmpty()) context.fail(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.Remove.NoEmojisFound),
                    Emotes.Error
                )
            }

            fetchedEmojis.forEach {
                if (it.guild == context.guild) {
                    it.delete().queue()
                }
            }

            context.reply(false) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.Remove.SuccessfullyRemovedEmoji(removedEmojisSize)),
                    Emotes.LoriHappyJumping
                )
            }
        }
    }
}