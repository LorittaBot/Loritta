package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentGenre
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentType
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.requests.GetChannelByIdRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.GetChannelByIdResponse

class CreateVideoGenreTypeScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: Long,
    private val contentGenre: LoriTuberContentGenre?,
    private val contentType: LoriTuberContentType?,
    private val contentLength: LoriTuberContentLength?
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

        val continueButton = loritta.interactivityManager.buttonForUser(
            user,
            false,
            ButtonStyle.PRIMARY,
            "Continuar",
            {
                disabled = contentType == null || contentGenre == null || contentLength == null
            }
        ) {
            command.switchScreen(
                CreateVideoEndScreen(
                    command,
                    user,
                    it.deferEdit(),
                    character,
                    channelId,
                    // Shouldn't be null here
                    contentGenre!!,
                    contentType!!,
                    contentLength!!
                )
            )
        }

        val viewChannelButton = loritta.interactivityManager.buttonForUser(
            user,
            false,
            ButtonStyle.PRIMARY,
            "Voltar",
            {
                emoji = Emoji.fromUnicode("\uD83C\uDF9E️")
            }
        ) {
            command.switchScreen(
                CreateVideoBeginningScreen(
                    command,
                    user,
                    it.deferEdit(),
                    character,
                    channel.id
                )
            )
        }

        hook.editOriginal(
            MessageEdit {
                embed {
                    title = "Criação de Vídeos [2/4]"

                    description = """Escolha o conteúdo do vídeo
                        |
                        |Combinação:
                        |**${contentGenre ?: "???"}** + **${contentType ?: "???"}** = **???** (Você nunca fez um vídeo com esta combinação!)
                        |**${contentGenre ?: "???"}** + **${contentType ?: "???"}** + **${contentLength ?: "???"}** = **???** (Você nunca fez um vídeo com esta combinação!)
                    """.trimMargin()
                }

                actionRow(
                    loritta.interactivityManager.stringSelectMenu(false, {
                        placeholder = "Gênero do Vídeo"

                        for (genre in LoriTuberContentGenre.values()) {
                            addOption(genre.name, genre.name)
                        }

                        val selectedContentGenre = contentGenre?.name
                        if (selectedContentGenre != null)
                            setDefaultValues(selectedContentGenre)
                    }) { it, values ->
                        command.switchScreen(
                            CreateVideoGenreTypeScreen(
                                command,
                                user,
                                it.deferEdit(),
                                character,
                                channel.id,
                                LoriTuberContentGenre.valueOf(values.first()),
                                contentType,
                                contentLength
                            )
                        )
                    }
                )

                actionRow(
                    loritta.interactivityManager.stringSelectMenu(false, {
                        placeholder = "Tipo do Vídeo"

                        for (type in LoriTuberContentType.values()) {
                            addOption(type.name, type.name)
                        }

                        val selectedContentType = contentType?.name
                        if (selectedContentType != null)
                            setDefaultValues(selectedContentType)
                    }) { it, values ->
                        command.switchScreen(
                            CreateVideoGenreTypeScreen(
                                command,
                                user,
                                it.deferEdit(),
                                character,
                                channel.id,
                                contentGenre,
                                LoriTuberContentType.valueOf(values.first()),
                                contentLength
                            )
                        )
                    }
                )

                actionRow(
                    loritta.interactivityManager.stringSelectMenu(false, {
                        placeholder = "Duração do Vídeo"

                        for (type in LoriTuberContentLength.values()) {
                            addOption(type.name, type.name)
                        }

                        val selectedContentType = contentLength?.name
                        if (selectedContentType != null)
                            setDefaultValues(selectedContentType)
                    }) { it, values ->
                        command.switchScreen(
                            CreateVideoGenreTypeScreen(
                                command,
                                user,
                                it.deferEdit(),
                                character,
                                channel.id,
                                contentGenre,
                                contentType,
                                LoriTuberContentLength.valueOf(values.first())
                            )
                        )
                    }
                )

                actionRow(continueButton)
                actionRow(viewChannelButton)
            }
        ).setReplace(true).await()
    }
}