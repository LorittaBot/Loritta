package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberMail
import net.perfectdreams.loritta.serializable.lorituber.requests.AcknowledgeMailRequest
import net.perfectdreams.loritta.serializable.lorituber.requests.GetChannelByIdRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.AcknowledgeMailResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.GetChannelByIdResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.GetMailResponse

class ReceivedMailScreen(command: LoriTuberCommand, user: User, hook: InteractionHook, val character: LoriTuberCommand.PlayerCharacter, val mailWrapper: GetMailResponse.MailWrapper, val oldScreen: LoriTuberScreen) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        val readMessageButton = loritta.interactivityManager.buttonForUser(
            user,
            false,
            ButtonStyle.PRIMARY,
            "Ler Mensagem",
            {
                emoji = Emoji.fromUnicode("\uD83D\uDCE7")
            }
        ) {
            val ackMailButton = loritta.interactivityManager.buttonForUser(
                user,
                false,
                ButtonStyle.PRIMARY,
                "Fechar Mensagem",
                {
                    emoji = Emoji.fromUnicode("\uD83D\uDCE7")
                }
            ) {
                val hook = it.deferEdit()
                // Acknowledge that we have read this mail
                sendLoriTuberRPCRequest<AcknowledgeMailResponse>(AcknowledgeMailRequest(mailWrapper.id))

                oldScreen.hook = hook
                command.switchScreen(oldScreen)
            }

            it.deferEdit()
                .editOriginal(
                    MessageEdit {
                        when (val mail = mailWrapper.mail) {
                            is LoriTuberMail.BeginnerChannelCreated -> {
                                val channel = sendLoriTuberRPCRequest<GetChannelByIdResponse>(GetChannelByIdRequest(mail.channelId))
                                    .channel

                                embed {
                                    title = "E-mail"

                                    description = """A competição na indústria dos influenciadores digitais continua a crescer, especialmente após a Loritta ter anunciado o novo plano de expansão para Starry Shores.
                                
                                Um dos novos moradores de Starry Shores, ${character.name}, criou o canal ${channel?.name} para tentar a sorte nesta indústria tão caótica.
                                
                                Será que ${character.name} tem o que é preciso para conseguir deixar a sua marca na internet e alcançar a fama?  
                            """.trimIndent()
                                }
                            }
                        }

                        actionRow(ackMailButton)
                    }
                ).setReplace(true).await()
        }

        hook.editOriginal(
            MessageEdit {
                embed {
                    title = "E-mail"

                    description = """Você recebeu uma mensagem!""".trimIndent()
                }

                actionRow(readMessageButton)
            }
        ).setReplace(true).await()
    }
}