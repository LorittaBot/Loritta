package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReferenceOrAttachment
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class ExpressionsCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Expressions
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.DISCORD, UUID.fromString("6392c773-3c42-4b18-92cf-145b3cbaa9b8")) {
        enableLegacyMessageSupport = true
        isGuildOnly = true

        integrationTypes = listOf(
            IntegrationType.GUILD_INSTALL
        )

        subcommandGroup(I18N_PREFIX.Sticker.Label, I18N_PREFIX.Sticker.Description) {
            subcommand(I18N_PREFIX.Sticker.Add.Label, I18N_PREFIX.Sticker.Add.Description, UUID.fromString("a3d3bcbf-17ba-4b35-a46e-288127972d07")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("addsticker")
                    add("adicionarfigurinha")
                }

                executor = StickerAddExecutor()
            }

            subcommand(I18N_PREFIX.Sticker.Remove.Label, I18N_PREFIX.Sticker.Remove.Description, UUID.fromString("7209c475-9269-4512-8624-3e89f814bf31")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("removesticker")
                    add("removerfigurinha")
                }

                executor = StickerRemoveExecutor()
            }
        }

        subcommandGroup(I18N_PREFIX.Emoji.Label, I18N_PREFIX.Emoji.Description) {
            subcommand(I18N_PREFIX.Emoji.Add.Label, I18N_PREFIX.Emoji.Add.Description, UUID.fromString("f0e2e530-3057-47b0-b2a9-0fa11b02f75a")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("addemoji")
                    add("adicionaremoji")
                }

                executor = EmojiAddExecutor()
            }

            subcommand(I18N_PREFIX.Emoji.Remove.Label, I18N_PREFIX.Emoji.Remove.Description, UUID.fromString("906952d3-a7f6-422e-a2c3-e0cb161f8717")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("removeemoji")
                    add("removeremoji")
                    add("delemoji")
                }

                executor = EmojiRemoveExecutor()
            }
        }
    }

    inner class StickerAddExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val stickerName = string("sticker_name", I18N_PREFIX.Sticker.Add.Options.Name)
            val stickerTags = optionalString("sticker_tags", I18N_PREFIX.Sticker.Add.Options.Tags)
            val stickerDescription = optionalString("sticker_description", I18N_PREFIX.Sticker.Add.Options.Description)
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
            val tags = args[options.stickerTags]?.split(", ") ?: listOf("none")

            val sticker = try {
                args[options.sticker].get(context, false)
            } catch(e: Exception) {
                null
            }

            if (sticker == null) context.fail(true) {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound),
                    Emotes.Error
                )
            }

            context.stickerFactory.addSticker(name, description, sticker, tags)

            context.reply(false) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.Sticker.Add.SuccessfullyAdded),
                    Emotes.LoriHappyJumping
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val name = args.getOrNull(0)
            val data = args.getOrNull(1)
            val tags = args.getOrNull(2)

            if (name == null) {
                context.explain()
            } else {
                return mapOf(
                    options.stickerName to name,
                    options.stickerTags to tags,
                    options.sticker to ImageReferenceOrAttachment(
                        dataValue = data,
                        attachment = context.event.message.attachments.firstOrNull()
                    )
                )
            }

            return null
        }
    }

    inner class StickerRemoveExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val stickerName = string("sticker_name", I18N_PREFIX.Sticker.Remove.Options.Name) {
                autocomplete { context ->
                    val stickerName = context.event.focusedOption.value

                    // There is no way that the guild could be null if the command is guild-only.
                    // So... non-null asserted.
                    val stickers = context.event.guild!!.stickers

                    if (stickerName.isBlank()) {
                        if (stickers.isEmpty()) {
                            return@autocomplete mapOf(
                                context.i18nContext.get(
                                    I18N_PREFIX.Sticker.Remove.NoStickersAvailable
                                ) to "empty"
                            )
                        } else {
                            return@autocomplete stickers.take(DiscordResourceLimits.Command.Options.ChoicesCount).associate { "${it.name} (${it.id})" to it.id }
                        }
                    } else {
                        val filteredStickers = stickers.filter { it.name.contains(stickerName, true) }
                        if (filteredStickers.isEmpty()) {
                            return@autocomplete mapOf(
                                context.i18nContext.get(
                                    I18N_PREFIX.Sticker.Remove.StickerNotFound
                                ) to stickerName
                            )
                        } else {
                            return@autocomplete filteredStickers.associate { "${it.name} (${it.id})" to it.id }
                        }
                    }
                }
            }
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

            val stickerId = args[options.stickerName].toLong()

            context.stickerFactory.removeSticker(stickerId)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val name = args.getOrNull(0)

            val stickers = context.guild.stickers

            val stickerId = stickers.firstOrNull { it.name == name }?.id

            if (name == null || stickerId == null) {
                context.explain()
            } else {
                return mapOf(options.stickerName to stickerId)
            }

            return null
        }
    }

    inner class EmojiAddExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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

            // We let the user add multiple emojis in a single message
            val name = args[options.emojiName]
            val emojiData = args[options.emojiData]

            if (emojiData.dataValue == null && emojiData.attachment == null) {
                // Here we will deal with existent emojis and a bunch of them.
                context.emojiFactory.addEmojiFromExistingEmoji(name)
            } else {
                // Here we will handle the name and the link for the emoji
                // Or make a copy of an existent emoji and change its name... huh
                val data = try {
                    args[options.emojiData].get(context, false)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }

                context.emojiFactory.addNewEmoji(name, data)
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val emojis = context.event.message.mentions.customEmojis
            val name = args.firstOrNull()

            if (emojis.isEmpty()) {
                if (name != null) {
                    val data = args.getOrNull(1)

                    return mapOf(
                        options.emojiName to name,
                        options.emojiData to ImageReferenceOrAttachment(
                            data,
                            context.event.message.attachments.firstOrNull()
                        )
                    )
                } else {
                    context.explain()
                    return null
                }
            } else {
                if (name != null && !name.contains("<")) {
                    return mapOf(
                        options.emojiName to name,
                        options.emojiData to ImageReferenceOrAttachment(
                            emojis.first().imageUrl,
                            null
                        )
                    )
                }

                return mapOf(
                    options.emojiName to emojis.joinToString(" ") { it.asMention },
                    options.emojiData to ImageReferenceOrAttachment(
                        null,
                        null
                    )
                )
            }
        }
    }

    inner class EmojiRemoveExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val emojiName = string("emoji_name", I18N_PREFIX.Emoji.Remove.Options.Name)
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

            context.emojiFactory.removeEmojis(args[options.emojiName])
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
            } else {
                return mapOf(options.emojiName to args.joinToString(" "))
            }

            return null
        }
    }
}