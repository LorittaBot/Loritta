package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.MessageCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.messageCommand
import net.perfectdreams.loritta.morenitta.messageverify.LoriMessageDataUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await

class SaveMessageCommand(val m: LorittaBot) {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Savemessage
    }

    class SaveMessagePublicCommand(val m: LorittaBot) : MessageCommandDeclarationWrapper {
        override fun command() = messageCommand(I18N_PREFIX.LabelPublic, CommandCategory.DISCORD, SaveMessageExecutor(m, true)) {
            this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        }
    }

    class SaveMessagePrivateCommand(val m: LorittaBot) : MessageCommandDeclarationWrapper {
        override fun command() = messageCommand(I18N_PREFIX.LabelPrivate, CommandCategory.DISCORD, SaveMessageExecutor(m, false)) {
            this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        }
    }

    class SaveMessageExecutor(val m: LorittaBot, val isPublic: Boolean) : LorittaMessageCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, message: Message) {
            val isEphemeral = !isPublic
            context.deferChannelMessage(isEphemeral)

            val savedMessage = LoriMessageDataUtils.convertMessageToSavedMessage(message)
            val finalImage = LoriMessageDataUtils.createSignedRenderedSavedMessage(m, savedMessage)

            // There's a difference here:
            // - Public Messages: Sent on the current channel, public reply
            // - Private Messages: Sent on the user's DMs, ephemeral reply
            val explanationMessages: InlineMessage<*>.() -> (Unit) = {
                styled(
                    context.i18nContext.get(I18N_PREFIX.YouReceivedACopyOfTheMessage(message.jumpUrl)),
                    Emotes.LoriHi
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.WhatYouCanDoWithIt(m.commandMentions.verifyMessageUrl)),
                    Emotes.LoriLurk
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.TheMessageIsASnapshot(m.commandMentions.verifyMessageUrl)),
                    Emotes.LoriReading
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.TamperingIsDetected(m.commandMentions.verifyMessageUrl)),
                    Emotes.LoriCoffee
                )
            }

            if (isPublic) {
                context.reply(isEphemeral) {
                    explanationMessages()

                    val fileName = LoriMessageDataUtils.createFileNameForSavedMessageImage(savedMessage)

                    files += FileUpload.fromData(finalImage, fileName)
                }
            } else {
                try {
                    val messageSent = context.user
                        .openPrivateChannel()
                        .await()
                        .sendMessage(
                            MessageCreate {
                                explanationMessages()

                                val fileName = LoriMessageDataUtils.createFileNameForSavedMessageImage(savedMessage)

                                files += FileUpload.fromData(finalImage, fileName)
                            }
                        )
                        .await()

                    // This should NEVER be null
                    val attachmentUrl = messageSent.attachments
                        .first()
                        .url

                    context.reply(isEphemeral) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.TheMessageCopyWasSentInYourDMs(attachmentUrl)),
                            Emotes.LoriHi
                        )
                    }
                } catch (e: ErrorResponseException) {
                    if (e.errorResponse == ErrorResponse.CANNOT_SEND_TO_USER) {
                        context.reply(isEphemeral) {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.YourDMsAreClosed),
                                Emotes.LoriSob
                            )
                        }
                        return
                    }
                    throw e
                }
            }
        }
    }
}