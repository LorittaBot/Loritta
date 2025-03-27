package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.requests.GetChannelByIdRequest
import net.perfectdreams.loritta.serializable.lorituber.requests.GetPendingVideosByChannelRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.GetChannelByIdResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.GetPendingVideosByChannelResponse

class ViewChannelScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: Long,
) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        if (command.checkMail(user, hook, character, this))
            return

        val channel = sendLoriTuberRPCRequest<GetChannelByIdResponse>(GetChannelByIdRequest(channelId))
            .channel

        if (channel == null) {
            // Channel does not exist! Maybe it was deleted?
            command.switchScreen(
                CreateChannelScreen(
                    command,
                    user,
                    hook,
                    character
                )
            )
            return
        }

        val pendingVideos =
            sendLoriTuberRPCRequest<GetPendingVideosByChannelResponse>(GetPendingVideosByChannelRequest(channelId))
                .pendingVideos

        val createOrContinueVideoButton = if (pendingVideos.isNotEmpty()) {
            val pendingVideo = pendingVideos.firstOrNull()

            loritta.interactivityManager.buttonForUser(
                user,
                false,
                ButtonStyle.PRIMARY,
                "Continuar vídeo",
                {
                    emoji = Emoji.fromUnicode("\uD83C\uDFAC")
                }
            ) {
                command.switchScreen(
                    CreateVideoBeginningScreen(
                        command,
                        user,
                        it.deferEdit(),
                        character,
                        channelId
                    )
                )
            }
        } else {
            loritta.interactivityManager.buttonForUser(
                user,
                false,
                ButtonStyle.PRIMARY,
                "Criar vídeo",
                {
                    emoji = Emoji.fromUnicode("\uD83C\uDFAC")
                }
            ) {
                command.switchScreen(
                    CreateVideoBeginningScreen(
                        command,
                        user,
                        it.deferEdit(),
                        character,
                        channelId
                    )
                )
            }
        }

        val viewMotivesButton = loritta.interactivityManager.buttonForUser(
            user,
            false,
            ButtonStyle.PRIMARY,
            "Voltar ao cafofo",
            {
                emoji = Emoji.fromUnicode("\uD83C\uDFE0")
            }
        ) {
            command.switchScreen(
                ViewMotivesScreen(
                    command,
                    user,
                    it.deferEdit(),
                    character
                )
            )
        }

        hook.editOriginal(
            MessageEdit {
                embed {
                    title = "Canal ${channel.name}"

                    field("ayaya", "yay!!!")
                }

                actionRow(createOrContinueVideoButton)
                actionRow(viewMotivesButton)
            }
        ).setReplace(true).await()
    }
}