package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberChannels
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberCharacters
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberPendingVideos
import net.perfectdreams.loritta.lorituber.LoriTuberPendingVideo
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens.ViewChannelScreen.ContinuePendingVideoResult
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class ViewPendingVideoScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: Long,
    val pendingVideoId: Long
) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        if (command.checkMail(user, hook, character, this))
            return

        val result = loritta.transaction {
            val channel = LoriTuberChannels.selectAll()
                .where {
                    LoriTuberChannels.id eq channelId
                }
                .firstOrNull()

            if (channel == null)
                return@transaction ViewPendingVideoResult.UnknownChannel

            val pendingVideos = LoriTuberPendingVideos.selectAll()
                .where {
                    LoriTuberPendingVideos.id eq pendingVideoId
                }
                .first()
                .let {
                    LoriTuberPendingVideo(
                        it[LoriTuberPendingVideos.id].value,
                        it[LoriTuberPendingVideos.contentCategory],
                        it[LoriTuberPendingVideos.contentLength],
                        it[LoriTuberPendingVideos.currentStage],
                        it[LoriTuberPendingVideos.currentStageProgressTicks],
                        it[LoriTuberPendingVideos.recordingScore],
                        it[LoriTuberPendingVideos.editingScore],
                        it[LoriTuberPendingVideos.thumbnailScore],
                        it[LoriTuberPendingVideos.videoResolution]
                    )
                }

            return@transaction ViewPendingVideoResult.PendingVideo(
                LoriTuberChannel(
                    channel[LoriTuberChannels.id].value,
                    channel[LoriTuberChannels.name]
                ),
                pendingVideos
            )
        }

        val viewMotivesButton = loritta.interactivityManager.buttonForUser(
            user,
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

        when (result) {
            ViewPendingVideoResult.UnknownChannel -> {
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

            is ViewPendingVideoResult.PendingVideo -> {
                val continueVideoButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Continuar Vídeo",
                    {
                        emoji = Emoji.fromUnicode("\uD83C\uDFAC")
                    }
                ) { context ->
                    val interactionHook = context.deferEdit()

                    val continuePendingVideoResult = loritta.transaction {
                        val character = LoriTuberCharacters.selectAll().where {
                            LoriTuberCharacters.id eq character.id
                        }.first()

                        val mood = listOf(
                            character[LoriTuberCharacters.energyNeed],
                            character[LoriTuberCharacters.hungerNeed],
                            character[LoriTuberCharacters.funNeed],
                            character[LoriTuberCharacters.hygieneNeed],
                            character[LoriTuberCharacters.bladderNeed],
                            character[LoriTuberCharacters.socialNeed]
                        ).average()

                        if (mood >= 50.0) {
                            LoriTuberCharacters.update({ LoriTuberCharacters.id eq character[LoriTuberCharacters.id] }) {
                                it[currentTask] = Json.encodeToString<LoriTuberTask>(
                                    LoriTuberTask.WorkingOnVideo(
                                        channelId,
                                        pendingVideoId
                                    )
                                )
                            }
                            return@transaction ContinuePendingVideoResult.Success
                        } else {
                            return@transaction ContinuePendingVideoResult.MoodTooLow
                        }
                    }

                    when (continuePendingVideoResult) {
                        ContinuePendingVideoResult.MoodTooLow -> {
                            context.reply(true) {
                                styled("Eu estou muito deprimido para continuar a trabalhar no vídeo")
                            }
                        }

                        ContinuePendingVideoResult.Success -> {
                            command.switchScreen(
                                ViewMotivesScreen(
                                    command,
                                    user,
                                    interactionHook,
                                    character
                                )
                            )
                        }
                    }
                }

                val jumpToNextButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Continuar para a Próxima Etapa",
                    {
                        emoji = Emoji.fromUnicode("\uD83C\uDFAC")
                    }
                ) { context ->
                    val interactionHook = context.deferEdit()

                    command.switchScreen(
                        ConfirmPendingVideoStageSwitchScreen(
                            command,
                            user,
                            interactionHook,
                            character,
                            channelId,
                            pendingVideoId
                        )
                    )
                }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Vídeo em Produção"

                            description = buildString {
                                appendLine("**Etapa Atual:** ${result.pendingVideo.currentStage} (${result.pendingVideo.currentStageProgressInTicks} ticks)")

                                // We actually can support multiple pending videos, but for now we only allow one
                                appendLine("**Gravação:** ${result.pendingVideo.recordingScore ?: "???"} pontos")
                                appendLine("**Edição:** ${result.pendingVideo.editingScore ?: "???"} pontos")
                                appendLine("**Thumbnail:** ${result.pendingVideo.thumbnailScore ?: "???"} pontos")
                                appendLine("**Resolução do Vídeo:** ${result.pendingVideo.videoResolution ?: "???"}")
                            }
                        }

                        actionRow(continueVideoButton, jumpToNextButton, viewMotivesButton)
                    }
                ).setReplace(true).await()
            }
        }
    }

    sealed class ViewPendingVideoResult {
        data object UnknownChannel : ViewPendingVideoResult()
        data class PendingVideo(
            val channel: LoriTuberChannel,
            val pendingVideo: LoriTuberPendingVideo
        ) : ViewPendingVideoResult()
    }
}