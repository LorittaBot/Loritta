package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.sticker.Sticker
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.RateLimitedException
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.ErrorResponse
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.newSticker

class MessageStickerCommand : MessageCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Stickersteal
    }

    override fun command() = messageCommand(
        I18N_PREFIX.GetStickerFromMessage,
        CommandCategory.DISCORD,
        MessageStickerCommandExecutor()
    ) {
        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)
    }

    inner class MessageStickerCommandExecutor : LorittaMessageCommandExecutor() {
        private fun Sticker.fixedIconUrl(size: Int): String {
            return when (formatType.extension) {
                "png" -> icon.getUrl(size)
                "gif" -> "https://media.discordapp.net/stickers/${id}.gif?size=$size"
                else -> icon.getUrl(size)
            }
        }

        private fun createStickerEmbed(sticker: Sticker) = Embed {
            title = "${sticker.name}.${sticker.formatType.extension}"
            image = sticker.fixedIconUrl(2048)
            color = 0x7289DA
            footer {
                name = "ID: ${sticker.id}"
            }
        }

        override suspend fun execute(context: ApplicationCommandContext, message: Message) {
            val sticker = message.stickers.firstOrNull()
                ?: context.fail(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.ThereIsntAnyStickerOnTheMessage),
                        Emotes.LoriHmpf
                    )
                }

            context.reply(true) {
                embeds.plusAssign(createStickerEmbed(sticker))

                if (context.guildOrNull == null) {
                    actionRow(
                        Button.link(
                            sticker.fixedIconUrl(2048),
                            context.i18nContext.get(
                                I18N_PREFIX.OpenInWeb
                            )
                        )
                    )
                } else {
                    if (context.member.permissions.any { it == Permission.MANAGE_GUILD_EXPRESSIONS }) {
                        actionRow(
                            Button.link(
                                sticker.fixedIconUrl(2048),
                                context.i18nContext.get(
                                    I18N_PREFIX.OpenInWeb
                                )
                            ),
                            context.loritta.interactivityManager.buttonForUser(
                                context.user,
                                ButtonStyle.PRIMARY,
                                context.i18nContext.get(I18N_PREFIX.AddStickerToTheServer)
                            ) {
                                try {
                                    it.guild.newSticker(
                                        context,
                                        sticker.name,
                                        "None",
                                        sticker.fixedIconUrl(2048),
                                        listOf("None")
                                    )
                                } catch (e: Exception) {
                                    it.deferAndEditOriginal {
                                        actionRow(
                                            Button.link(
                                                sticker.fixedIconUrl(2048),
                                                context.i18nContext.get(
                                                    I18N_PREFIX.OpenInWeb
                                                )
                                            ),
                                            Button.danger("-", it.i18nContext.get(GuildCommand.I18N_PREFIX.Sticker.Add.ErrorWhileAdding)).asDisabled()
                                        )
                                    }

                                    when (e) {
                                        is CommandException -> throw e
                                        is RateLimitedException -> {
                                            context.reply(true) {
                                                styled(
                                                    context.i18nContext.get(I18nKeysData.Commands.Command.Guild.Sticker.Add.RateLimitExceeded),
                                                    Emotes.LoriHmpf
                                                )
                                            }
                                            return@buttonForUser
                                        }
                                        is ErrorResponseException -> {
                                            when (e.errorResponse) {
                                                ErrorResponse.FILE_UPLOAD_MAX_SIZE_EXCEEDED -> {
                                                    it.reply(true) {
                                                        styled(
                                                            context.i18nContext.get(
                                                                GuildCommand.I18N_PREFIX.Sticker.Add.FileUploadMaxSizeExceeded
                                                            ),
                                                            Emotes.Error
                                                        )
                                                    }
                                                    return@buttonForUser
                                                }

                                                ErrorResponse.MAX_STICKERS -> {
                                                    it.reply(true) {
                                                        styled(
                                                            context.i18nContext.get(
                                                                GuildCommand.I18N_PREFIX.Sticker.Add.MaxStickersReached
                                                            ),
                                                            Emotes.Error
                                                        )
                                                    }
                                                    return@buttonForUser
                                                }

                                                ErrorResponse.INVALID_FILE_UPLOADED, ErrorResponse.INVALID_FORM_BODY -> {
                                                    it.reply(true) {
                                                        styled(
                                                            context.i18nContext.get(
                                                                GuildCommand.I18N_PREFIX.Sticker.Add.InvalidUrl
                                                            ),
                                                            Emotes.Error
                                                        )
                                                    }
                                                    return@buttonForUser
                                                }

                                                else -> it.reply(true) {
                                                    styled(
                                                        context.i18nContext.get(
                                                            I18nKeysData.Commands.ErrorWhileExecutingCommand(
                                                                Emotes.LoriRage,
                                                                Emotes.LoriSob,
                                                                e
                                                            )
                                                        ),
                                                        Emotes.Error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                it.deferAndEditOriginal {
                                    actionRow(
                                        Button.link(
                                            sticker.fixedIconUrl(2048),
                                            context.i18nContext.get(
                                                I18N_PREFIX.OpenInWeb
                                            )
                                        ),
                                        Button.success("-", it.i18nContext.get(I18N_PREFIX.StickerAddedSuccessfully)).asDisabled()
                                    )
                                }

                                it.reply(true) {
                                    styled(
                                        it.i18nContext.get(I18N_PREFIX.StickerAddedSuccessfully),
                                        Emotes.LoriHappy
                                    )
                                }
                            }
                        )
                    } else {
                        actionRow(
                            Button.link(
                                sticker.fixedIconUrl(2048),
                                context.i18nContext.get(
                                    I18N_PREFIX.OpenInWeb
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}