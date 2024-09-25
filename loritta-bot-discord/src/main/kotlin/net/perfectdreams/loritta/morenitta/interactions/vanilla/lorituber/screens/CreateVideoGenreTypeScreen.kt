package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberChannels
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentVibes
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import org.jetbrains.exposed.sql.selectAll

class CreateVideoGenreTypeScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: Long,
    private val contentCategory: LoriTuberVideoContentCategory?,
    private val contentLength: LoriTuberContentLength?
) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        val result = loritta.transaction {
            val channel = LoriTuberChannels.selectAll()
                .where {
                    LoriTuberChannels.id eq channelId
                }
                .firstOrNull()

            if (channel == null)
                return@transaction CreateVideoGenreTypeResult.UnknownChannel

            return@transaction CreateVideoGenreTypeResult.Channel(
                LoriTuberChannel(
                    channel[LoriTuberChannels.id].value,
                    channel[LoriTuberChannels.name]
                )
            )
        }

        when (result) {
            CreateVideoGenreTypeResult.UnknownChannel -> {
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
            is CreateVideoGenreTypeResult.Channel -> {
                val continueButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Continuar",
                    {
                        disabled = contentCategory == null || contentLength == null
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
                            contentLength!!,
                            mapOf(),
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
                                        LoriTuberVideoContentCategory.ANIMATION -> {}
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
                                appendLine()
                                appendLine("Duração: $contentLength")
                            }
                        }

                        actionRow(
                            loritta.interactivityManager.stringSelectMenu({
                                placeholder = "Categoria do Vídeo"

                                for (genre in LoriTuberVideoContentCategory.values()) {
                                    addOption(genre.name, genre.name)
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
                                        LoriTuberVideoContentCategory.valueOf(values.first()),
                                        contentLength
                                    )
                                )
                            }
                        )

                        actionRow(
                            loritta.interactivityManager.stringSelectMenu({
                                placeholder = "Duração do Vídeo"

                                for (type in LoriTuberContentLength.entries) {
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
                                        result.channel.id,
                                        contentCategory,
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
    }

    sealed class CreateVideoGenreTypeResult {
        data object UnknownChannel : CreateVideoGenreTypeResult()
        data class Channel(val channel: LoriTuberChannel) : CreateVideoGenreTypeResult()
    }
}