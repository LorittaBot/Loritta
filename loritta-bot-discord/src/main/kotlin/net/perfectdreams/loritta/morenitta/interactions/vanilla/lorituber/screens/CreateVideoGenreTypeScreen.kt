package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentVibes
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelByIdRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelByIdResponse
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import java.util.*

class CreateVideoGenreTypeScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: UUID,
    private val contentCategory: LoriTuberVideoContentCategory?,
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
            }
            is GetChannelByIdResponse.Success -> {
                val continueButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Continuar",
                    {
                        disabled = contentCategory == null
                    }
                ) {
                    command.switchScreen(
                        CreateVideoVibesScreen(
                            command,
                            user,
                            it.deferEdit(),
                            character,
                            channelId,
                            // Shouldn't be null here
                            contentCategory!!,
                            LoriTuberVibes(0),
                            LoriTuberVideoContentVibes.VIBE1
                        )
                    )
                }

                val viewChannelButton = loritta.interactivityManager.buttonForUser(
                    user,
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
                            result.channel.id
                        )
                    )
                }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Criação de Vídeos [2/4]"

                            description = buildString {
                                appendLine("Escolha o conteúdo do vídeo")
                                appendLine()
                                appendLine("$contentCategory")
                                if (contentCategory != null) {
                                    when (contentCategory) {
                                        LoriTuberVideoContentCategory.ANIMATION_AND_ART -> {}
                                        LoriTuberVideoContentCategory.GAMES -> {}
                                        LoriTuberVideoContentCategory.COMEDY -> {}
                                        LoriTuberVideoContentCategory.BEAUTY -> {}
                                        LoriTuberVideoContentCategory.EDUCATION -> {
                                            appendLine("É a sua chance de usar a fórmula de Bhaskara para alguma coisa útil na sua vida")
                                        }
                                        LoriTuberVideoContentCategory.TECHNOLOGY -> {}
                                        LoriTuberVideoContentCategory.REAL_LIFE -> {}
                                        LoriTuberVideoContentCategory.DOCUMENTARY -> {}
                                        LoriTuberVideoContentCategory.FINANCE -> {}
                                        LoriTuberVideoContentCategory.POLITICS -> {}
                                    }
                                }
                            }
                        }

                        actionRow(
                            loritta.interactivityManager.stringSelectMenu({
                                placeholder = "Categoria do Vídeo"

                                for (genre in LoriTuberVideoContentCategory.entries) {
                                    val categoryLevel = result.channel.contentLevels.getOrDefault(genre, 1)
                                    addOption("${genre.name} [Nível $categoryLevel]", genre.name)
                                }

                                val selectedContentGenre = contentCategory?.name
                                if (selectedContentGenre != null)
                                    setDefaultValues(selectedContentGenre)
                            }) { it, values ->
                                command.switchScreen(
                                    CreateVideoGenreTypeScreen(
                                        command,
                                        user,
                                        it.deferEdit(),
                                        character,
                                        result.channel.id,
                                        LoriTuberVideoContentCategory.valueOf(values.first())
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
    }

    sealed class CreateVideoGenreTypeResult {
        data object UnknownChannel : CreateVideoGenreTypeResult()
        data class Channel(val channel: LoriTuberChannel) : CreateVideoGenreTypeResult()
    }
}