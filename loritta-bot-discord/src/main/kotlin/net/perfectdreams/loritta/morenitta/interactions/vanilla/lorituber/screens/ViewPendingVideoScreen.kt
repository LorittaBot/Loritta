package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.lorituber.LoriTuberPendingVideo
import net.perfectdreams.loritta.lorituber.LoriTuberVideoStage
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberPendingVideo
import net.perfectdreams.loritta.lorituber.rpc.packets.*
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel

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

        val result = sendLoriTuberRPCRequestNew<GetPendingVideoByIdResponse>(
            GetPendingVideoByIdRequest(
                channelId,
                pendingVideoId
            )
        )
        /* val result = loritta.transaction {
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
        } */

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
            GetPendingVideoByIdResponse.UnknownChannel -> {
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

            GetPendingVideoByIdResponse.UnknownPendingVideo -> {
                // Pending Video does not exist! Maybe it was deleted?
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

            is GetPendingVideoByIdResponse.Success -> {
                val continueVideoButtons = mutableListOf<Button>()
                data class StageWrapper(
                    val stage: LoriTuberVideoStage,
                    val stageData: NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData,
                )
                val stages = listOf(
                    StageWrapper(LoriTuberVideoStage.RECORDING, result.pendingVideo.recordingStage),
                    StageWrapper(LoriTuberVideoStage.EDITING, result.pendingVideo.editingStage),
                    StageWrapper(LoriTuberVideoStage.RENDERING, result.pendingVideo.renderingStage),
                    StageWrapper(LoriTuberVideoStage.THUMBNAIL, result.pendingVideo.thumbnailStage)
                )

                var finishedCount = 0
                for (stage in stages) {
                    when (stage.stageData) {
                        is NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Finished -> {
                            finishedCount++
                        }
                        is NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.InProgress -> {
                            continueVideoButtons.add(
                                loritta.interactivityManager.buttonForUser(
                                    user,
                                    ButtonStyle.PRIMARY,
                                    "Trabalhar no Vídeo (${stage.stage})",
                                    {
                                        emoji = Emoji.fromUnicode("\uD83C\uDFAC")
                                    }
                                ) { context ->
                                    val interactionHook = context.deferEdit()

                                    val continuePendingVideoResult = sendLoriTuberRPCRequestNew<StartWorkingOnPendingVideoResponse>(
                                        StartWorkingOnPendingVideoRequest(
                                            character.id,
                                            channelId,
                                            pendingVideoId,
                                            stage.stage
                                        )
                                    )

                                    when (continuePendingVideoResult) {
                                        StartWorkingOnPendingVideoResponse.MoodTooLow -> {
                                            context.reply(true) {
                                                styled("Eu estou muito deprimido para continuar a trabalhar no vídeo")
                                            }
                                        }

                                        StartWorkingOnPendingVideoResponse.Success -> {
                                            command.switchScreen(
                                                ViewMotivesScreen(
                                                    command,
                                                    user,
                                                    interactionHook,
                                                    character
                                                )
                                            )
                                        }

                                        StartWorkingOnPendingVideoResponse.UnknownChannel -> TODO()
                                        StartWorkingOnPendingVideoResponse.UnknownPendingVideo -> TODO()
                                    }
                                }
                            )
                        }
                        NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Unavailable -> {}
                    }
                }

                if (finishedCount == stages.size) {
                    continueVideoButtons.add(
                        loritta.interactivityManager.buttonForUser(
                            user,
                            ButtonStyle.PRIMARY,
                            "Publicar Vídeo",
                            {
                                emoji = Emoji.fromUnicode("\uD83C\uDFAC")
                            }
                        ) { context ->
                            val videoTitleOption = modalString("Título do Vídeo", TextInputStyle.SHORT)

                            context.sendModal(
                                "Vídeo",
                                listOf(ActionRow.of(videoTitleOption.toJDA()))
                            ) { it, args ->
                                val videoTitle = args[videoTitleOption]

                                val hook = it.deferEdit()
                                val continuePendingVideoResult = sendLoriTuberRPCRequestNew<FinishPendingVideoResponse>(
                                    FinishPendingVideoRequest(
                                        channelId,
                                        result.pendingVideo.id,
                                        videoTitle
                                    )
                                )

                                when (continuePendingVideoResult) {
                                    FinishPendingVideoResponse.Success -> {
                                        command.switchScreen(
                                            ViewMotivesScreen(
                                                command,
                                                user,
                                                hook.jdaHook,
                                                character
                                            )
                                        )
                                    }

                                    FinishPendingVideoResponse.UnknownChannel -> TODO()
                                    FinishPendingVideoResponse.UnknownPendingVideo -> TODO()
                                }
                            }
                        }
                    )
                }

                /* val jumpToNextButton = loritta.interactivityManager.buttonForUser(
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
                } */

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Vídeo em Produção"

                            description = buildString {
                                // appendLine("**Etapa Atual:** ${result.pendingVideo.currentStage} (${result.pendingVideo.currentStageProgressTicks} ticks)")

                                // We actually can support multiple pending videos, but for now we only allow one
                                appendLine("**Gravação:** ${(result.pendingVideo.recordingStage as? NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Finished)?.score ?: "???"} pontos")
                                appendLine("**Edição:** ${(result.pendingVideo.editingStage as? NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Finished)?.score ?: "???"} pontos")
                                appendLine("**Thumbnail:** ${(result.pendingVideo.thumbnailStage as? NetworkLoriTuberPendingVideo.LoriTuberPendingVideoStageData.Finished)?.score ?: "???"} pontos")
                                // appendLine("**Resolução do Vídeo:** ${result.pendingVideo.videoResolution ?: "???"}")
                            }
                        }

                        actionRow(continueVideoButtons)
                        actionRow(viewMotivesButton)
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