package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.sticker.StickerItem
import net.dv8tion.jda.api.exceptions.RateLimitedException
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils

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
        override suspend fun execute(context: ApplicationCommandContext, message: Message) {
            val sticker = message.stickers.firstOrNull()
                ?: context.fail(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.ThereIsntAnyStickerOnTheMessage),
                        Emotes.LORI_HMPF
                    )
                }

            context.reply(true) {
                embeds.plusAssign(createStickerEmbed(sticker))

                if (context.guildOrNull == null) {
                    actionRow(
                        Button.link(
                            sticker.iconUrl + "?size=2048",
                            context.i18nContext.get(
                                I18N_PREFIX.OpenInWeb
                            )
                        )
                    )
                } else {
                    if (context.member.permissions.any { it == Permission.MANAGE_GUILD_EXPRESSIONS }) {
                        actionRow(
                            Button.link(
                                sticker.iconUrl + "?size=2048",
                                context.i18nContext.get(
                                    I18N_PREFIX.OpenInWeb
                                )
                            ),
                            context.loritta.interactivityManager.buttonForUser(
                                context.user,
                                ButtonStyle.PRIMARY,
                                context.i18nContext.get(I18N_PREFIX.AddStickerToTheServer)
                            ) {
                                val image = (LorittaUtils.downloadFile(context.loritta, sticker.iconUrl, 5000) ?: context.fail(true) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.CouldntDownloadTheSticker),
                                        Emotes.LORI_HMPF
                                    )
                                }).readAllBytes()

                                val parsedSticker = FileUpload.fromData(image, "sticker.${sticker.formatType.extension}")

                                try {
                                    it.guild.createSticker(
                                        sticker.name,
                                        "None",
                                        parsedSticker,
                                        "None"
                                    ).submit(false).await()
                                } catch (e: RateLimitedException) {
                                    context.fail(true) {
                                        styled(
                                            context.i18nContext.get(I18nKeysData.Commands.Command.Guild.Sticker.Add.RateLimitExceeded),
                                            Emotes.LORI_HMPF
                                        )
                                    }
                                }

                                it.reply(true) {
                                    styled(
                                        it.i18nContext.get(I18N_PREFIX.StickerAddedSuccessfully),
                                        Emotes.LORI_HAPPY
                                    )
                                }
                            }
                        )
                    } else {
                        actionRow(
                            Button.link(
                                sticker.iconUrl + "?size=2048",
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

    suspend fun createStickerEmbed(sticker: StickerItem) = Embed {
        title = sticker.name
        image = sticker.iconUrl + "?size=2048"
        color = 0x7289DA
        footer {
            name = "ID: ${sticker.id}"
        }
    }
}