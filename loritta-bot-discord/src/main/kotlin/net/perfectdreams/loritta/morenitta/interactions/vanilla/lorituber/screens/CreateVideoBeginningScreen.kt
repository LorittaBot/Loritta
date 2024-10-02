package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelByIdRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelByIdResponse
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel

class CreateVideoBeginningScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: Long,
) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        val result = sendLoriTuberRPCRequestNew<GetChannelByIdResponse>(GetChannelByIdRequest(channelId))

        when (result) {
            GetChannelByIdResponse.UnknownChannel -> {
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
            is GetChannelByIdResponse.Success -> {
                val viewChannelButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Voltar",
                    {
                        emoji = Emoji.fromUnicode("\uD83C\uDF9E️")
                    }
                ) {
                    command.switchScreen(
                        ViewChannelScreen(
                            command,
                            user,
                            it.deferEdit(),
                            character,
                            result.channel.id
                        )
                    )
                }

                val createVideoButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Continuar"
                ) {
                    command.switchScreen(
                        CreateVideoGenreTypeScreen(
                            command,
                            user,
                            it.deferEdit(),
                            character,
                            result.channel.id,
                            null
                        )
                    )
                }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Criação de Vídeos [1/4]"

                            description = """Vamos começar a criar conteúdo para o seu canal!
                        |
                        |O processo de criação de vídeo é separado em quatro etapas:
                        |- Gravação
                        |- Edição
                        |- Renderização
                        |- Thumbnail
                    """.trimMargin()
                        }

                        actionRow(createVideoButton)

                        actionRow(viewChannelButton)
                    }
                ).setReplace(true).await()
            }
        }
    }

    sealed class CreateVideoBeginningResult {
        data object UnknownChannel : CreateVideoBeginningResult()
        data class Channel(val channel: LoriTuberChannel) : CreateVideoBeginningResult()
    }
}