package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.RateLimitedException
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo

class GuildCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Guild
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.DISCORD) {
        enableLegacyMessageSupport = true
        isGuildOnly = true

        integrationTypes = listOf(
                Command.IntegrationType.GUILD_INSTALL
        )

        subcommandGroup(I18N_PREFIX.Sticker.Label, I18N_PREFIX.Sticker.Description) {
            subcommand(I18N_PREFIX.Sticker.Add.Label, I18N_PREFIX.Sticker.Add.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("addsticker")
                    add("adicionarfigurinha")
                }

                executor = GuildStickerAddExecutor()
            }

            subcommand(I18N_PREFIX.Sticker.Remove.Label, I18N_PREFIX.Sticker.Remove.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("removesticker")
                    add("removerfigurinha")
                }

                executor = GuildStickerRemoveExecutor()
            }
        }

        subcommandGroup(I18N_PREFIX.Emoji.Label, I18N_PREFIX.Emoji.Description) {
            subcommand(I18N_PREFIX.Emoji.Add.Label, I18N_PREFIX.Emoji.Add.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("addemoji")
                    add("adicionaremoji")
                }

                executor = GuildEmojiAddExecutor()
            }

            subcommand(I18N_PREFIX.Emoji.Remove.Label, I18N_PREFIX.Emoji.Remove.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("removeemoji")
                    add("removeremoji")
                }

                executor = GuildEmojiRemoveExecutor()
            }
        }
    }

    inner class GuildStickerAddExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val stickerName = string("sticker_name", I18N_PREFIX.Sticker.Add.Options.Name)
            val stickerDescription = string("sticker_description", I18N_PREFIX.Sticker.Add.Options.Description)
            val stickerTags = string("sticker_tags", I18N_PREFIX.Sticker.Add.Options.Tags)
            val sticker = imageReferenceOrAttachment("sticker", I18N_PREFIX.Sticker.Add.Options.ImageData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (!context.member.permissions.any { it == Permission.MANAGE_GUILD_EXPRESSIONS }) context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18nKeysData.Commands.UserDoesntHavePermissionDiscord(Permission.MANAGE_GUILD_EXPRESSIONS)
                    ),
                    Emotes.Error
                )
            }

            val name = args[options.stickerName]
            val description = args[options.stickerDescription] ?: context.i18nContext.get(
                I18N_PREFIX.Sticker.Add.DefaultDescription
            )
            val tags = args[options.stickerTags].split(", ")

            val sticker = try {
                args[options.sticker].get(context, false)
            } catch(e: Exception) {
                null
            }

            if (sticker == null) context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18nKeysData.Commands.NoValidImageFound
                    ),
                    Emotes.Error
                )
            }

            if (name.length < 2 || name.length > 30) context.fail(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.Sticker.Add.OutOfBoundsName)
                )
            }

            if (description.length < 2 || description.length > 100) context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Sticker.Add.OutOfBoundsDescription
                    ),
                    Emotes.Error
                )
            }

            context.deferChannelMessage(false)

            try {
                val image = (LorittaUtils.downloadFile(context.loritta, sticker, 5000) ?: context.fail(true) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.Sticker.Add.InvalidUrl
                        ),
                        Emotes.Error
                    )
                }).readAllBytes()

                val imageInfo = SimpleImageInfo(image)
                val imageType = imageInfo.mimeType!!.split("/")[1]

                try {
                    context.guild.createSticker(
                        name,
                        description,
                        FileUpload.fromData(image, "sticker.$imageType"),
                        tags
                    ).submit(false).await()
                } catch(e: RateLimitedException) {
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(
                                I18N_PREFIX.Sticker.Add.RateLimitExceeded
                            ),
                            Emotes.Error
                        )
                    }
                }

                context.reply(false) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.Sticker.Add.SuccessfullyAdded
                        ),
                        Emotes.LoriHappyJumping
                    )
                }
            } catch (e: ErrorResponseException) {
                e.printStackTrace()

                when (e.errorResponse) {
                    ErrorResponse.FILE_UPLOAD_MAX_SIZE_EXCEEDED -> {
                        context.fail(true) {
                            styled(
                                context.i18nContext.get(
                                    I18N_PREFIX.Sticker.Add.FileUploadMaxSizeExceeded
                                ),
                                Emotes.Error
                            )
                        }
                    }
                    ErrorResponse.MAX_STICKERS -> {
                        context.fail(true) {
                            styled(
                                context.i18nContext.get(
                                    I18N_PREFIX.Sticker.Add.MaxStickersReached,
                                ),
                                Emotes.Error
                            )
                        }
                    }
                    else -> {
                        context.fail(true) {
                            styled(
                                context.i18nContext.get(
                                    I18nKeysData.Commands.ErrorWhileExecutingCommand(
                                        Emotes.LoriRage,
                                        Emotes.LoriSob,
                                        e.message!!
                                    )
                                ),
                                Emotes.Error
                            )
                        }
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val name = args.getOrNull(0)
            val data = args.getOrNull(1)
            val tags = args.getOrNull(2)


            if (name == null || data == null || tags == null) {
                context.explain()
            } else {
                return mapOf(
                    options.stickerName to name,
                    options.stickerTags to tags,
                    options.sticker to ImageReference(
                        dataValue = data,
                        attachment = context.event.message.attachments.firstOrNull()
                    )
                )
            }

            return null
        }
    }

    inner class GuildStickerRemoveExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (!context.member.permissions.any { it == Permission.MANAGE_GUILD_EXPRESSIONS }) context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18nKeysData.Commands.UserDoesntHavePermissionDiscord(Permission.MANAGE_GUILD_EXPRESSIONS)
                    ),
                    Emotes.Error
                )
            }

            val stickers = context.guild.stickers

            if (stickers.isEmpty()) context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Sticker.Remove.NoStickersAvailable
                    ),
                    Emotes.Error
                )
            }

            context.deferChannelMessage(false)

            context.reply(false) {
                apply(
                    GuildExpressionsManagerExecutor.createRemoveStickerMessage(
                            context,
                            stickers
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = mapOf()
    }

    inner class GuildEmojiAddExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val emojiName = string("emoji_name", I18N_PREFIX.Emoji.Add.Options.Name)
            val emojiData = imageReferenceOrAttachment("emoji", I18N_PREFIX.Emoji.Add.Options.ImageData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (!context.member.permissions.any { it == Permission.MANAGE_GUILD_EXPRESSIONS }) context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18nKeysData.Commands.UserDoesntHavePermissionDiscord(Permission.MANAGE_GUILD_EXPRESSIONS)
                    ),
                    Emotes.Error
                )
            }

            val name = args[options.emojiName]
            val data = try {
                args[options.emojiData].get(context, false)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (data == null) context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18nKeysData.Commands.NoValidImageFound
                    ),
                    Emotes.Error
                )
            }

            context.deferChannelMessage(false)

            try {
                val image = LorittaUtils.downloadFile(context.loritta, data, 5000) ?: context.fail(true) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.Emoji.Add.InvalidUrl
                        ),
                        Emotes.Error
                    )
                }

                val addedEmoji = try {
                    context.guild.createEmoji(
                        name,
                        Icon.from(image)
                    ).submit(false).await()
                } catch(e: RateLimitedException) {
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(
                                I18N_PREFIX.Emoji.Add.RateLimitExceeded
                            ),
                            Emotes.Error
                        )
                    }
                }

                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Emoji.Add.SuccessfullyAdded),
                        addedEmoji.asMention
                    )
                }
            } catch (e: ErrorResponseException) {
                e.printStackTrace()

                when (e.errorResponse) {
                    ErrorResponse.FILE_UPLOAD_MAX_SIZE_EXCEEDED -> context.fail(true) {
                        styled(
                            context.i18nContext.get(
                                I18N_PREFIX.Emoji.Add.FileUploadMaxSizeExceeded
                            ),
                            Emotes.Error
                        )
                    }

                    ErrorResponse.MAX_EMOJIS -> context.fail(true) {
                        styled(
                            context.i18nContext.get(
                                I18N_PREFIX.Emoji.Add.MaxStaticEmojisLimitReached,
                            ),
                            Emotes.Error
                        )
                    }

                    ErrorResponse.MAX_ANIMATED_EMOJIS -> context.fail(true) {
                        styled(
                            context.i18nContext.get(
                                I18N_PREFIX.Emoji.Add.MaxAnimatedEmojisLimitReached
                            ),
                            Emotes.Error
                        )
                    }

                    else -> context.fail(true) {
                        styled(
                            context.i18nContext.get(
                                I18nKeysData.Commands.ErrorWhileExecutingCommand(
                                    Emotes.LoriRage,
                                    Emotes.LoriSob,
                                    e.message!!
                                )
                            ),
                            Emotes.Error
                        )
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val name = args.getOrNull(0)
            val data = args.getOrNull(1)

            if (name == null || data == null) {
                context.explain()
            } else {
                return mapOf(
                    options.emojiName to name,
                    options.emojiData to ImageReference(
                        dataValue = data,
                        attachment = context.event.message.attachments.firstOrNull()
                    )
                )
            }

            return null
        }
    }

    inner class GuildEmojiRemoveExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (!context.member.permissions.any { it == Permission.MANAGE_GUILD_EXPRESSIONS }) context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18nKeysData.Commands.UserDoesntHavePermissionDiscord(Permission.MANAGE_GUILD_EXPRESSIONS)
                    ),
                    Emotes.Error
                )
            }

            val emojis = context.guild.emojis

            if (emojis.isEmpty()) context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Emoji.Remove.NoEmojisAvailable
                    ),
                    Emotes.Error
                )
            }

            context.deferChannelMessage(false)

            context.reply(false) {
                apply(
                    GuildExpressionsManagerExecutor.createRemoveEmojiMessage(context, emojis)
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = mapOf()
    }
}