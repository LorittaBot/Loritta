package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentGenre
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentType
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask
import net.perfectdreams.loritta.serializable.lorituber.requests.CreatePendingVideoRequest
import net.perfectdreams.loritta.serializable.lorituber.requests.GetChannelByIdRequest
import net.perfectdreams.loritta.serializable.lorituber.requests.StartTaskRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.CreatePendingVideoResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.GetChannelByIdResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.StartTaskResponse

class CreateVideoEndScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: Long,
    private val contentGenre: LoriTuberContentGenre,
    private val contentType: LoriTuberContentType,
    private val contentLength: LoriTuberContentLength
) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
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

        val createVideoButton = loritta.interactivityManager.buttonForUser(
            user,
            false,
            ButtonStyle.PRIMARY,
            "Criar"
        ) {
            val hook = it.deferEdit()

            when (val response = sendLoriTuberRPCRequest<CreatePendingVideoResponse>(CreatePendingVideoRequest(character.id, channelId, contentGenre, contentType, contentLength))) {
                is CreatePendingVideoResponse.Success -> {
                    // Successfully created pending video!
                    when (sendLoriTuberRPCRequest<StartTaskResponse>(StartTaskRequest(character.id, LoriTuberTask.WorkingOnVideo(channelId, response.videoId)))) {
                        is StartTaskResponse.Success -> {
                            command.switchScreen(
                                ViewMotivesScreen(
                                    command,
                                    user,
                                    hook,
                                    LoriTuberCommand.PlayerCharacter(character.id, character.name, 100.0, 100.0)
                                )
                            )
                        }
                        is StartTaskResponse.CharacterIsAlreadyDoingAnotherTask -> it.deferEdit().editOriginal(
                            MessageEdit {
                                content = "Você já está fazendo outra tarefa!"
                            }
                        ).setReplace(true).await()
                    }
                }
                is CreatePendingVideoResponse.CharacterIsAlreadyDoingAnotherVideo ->  it.deferEdit().editOriginal(
                    MessageEdit {
                        content = "Você já tem um vídeo pendente sendo feito!"
                    }
                ).setReplace(true).await()
            }
        }

        hook.editOriginal(
            MessageEdit {
                embed {
                    title = "Criação de Vídeos [4/4]"

                    description = """Começar a criar o vídeo?""".trimMargin()
                }

                actionRow(createVideoButton)
            }
        ).setReplace(true).await()
    }
}