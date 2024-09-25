package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberChannels
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberPendingVideos
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberServerInfos
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberVideos
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoStage
import net.perfectdreams.loritta.lorituber.LoriTuberPendingVideo
import net.perfectdreams.loritta.lorituber.LoriTuberServer
import net.perfectdreams.loritta.lorituber.ServerInfo
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class ConfirmPendingVideoStageSwitchScreen(
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
                val newStage = when (result.pendingVideo.currentStage) {
                    LoriTuberVideoStage.RECORDING -> LoriTuberVideoStage.EDITING
                    LoriTuberVideoStage.EDITING -> LoriTuberVideoStage.THUMBNAIL
                    LoriTuberVideoStage.THUMBNAIL -> LoriTuberVideoStage.RENDERING
                    LoriTuberVideoStage.RENDERING -> LoriTuberVideoStage.FINISHED
                    LoriTuberVideoStage.FINISHED -> error("Unsupported")
                }

                // TODO: You can only change if the current score is not null
                val switchStageButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Confirmar Troca para $newStage",
                    {
                        emoji = Emoji.fromUnicode("\uD83C\uDFE0")
                    }
                ) {
                    loritta.transaction {
                        if (newStage == LoriTuberVideoStage.FINISHED) {
                            // It's finished! Switch it to be a REAL video with REAL aspirations and stuffz
                            val serverInfo = loritta.transaction {
                                LoriTuberServerInfos.selectAll()
                                    .where { LoriTuberServerInfos.type eq LoriTuberServer.GENERAL_INFO_KEY }
                                    .first()
                                    .get(LoriTuberServerInfos.data)
                                    .let { Json.decodeFromString<ServerInfo>(it) }
                            }

                            val pendingVideoData = LoriTuberPendingVideos.selectAll()
                                .where {
                                    LoriTuberPendingVideos.id eq pendingVideoId
                                }
                                .first()

                            // The video has been posted (wowzers)
                            LoriTuberVideos.insert {
                                it[LoriTuberVideos.owner] = character.id
                                it[LoriTuberVideos.channel] = channelId
                                it[LoriTuberVideos.postedAtTicks] = serverInfo.currentTick
                                it[LoriTuberVideos.contentCategory] = pendingVideoData[LoriTuberPendingVideos.contentCategory]
                                it[LoriTuberVideos.contentLength] = pendingVideoData[LoriTuberPendingVideos.contentLength]
                                it[LoriTuberVideos.recordingScore] = pendingVideoData[LoriTuberPendingVideos.recordingScore]!!
                                it[LoriTuberVideos.editingScore] = pendingVideoData[LoriTuberPendingVideos.editingScore]!!
                                it[LoriTuberVideos.thumbnailScore] = pendingVideoData[LoriTuberPendingVideos.thumbnailScore]!!
                                it[LoriTuberVideos.vibe1] = pendingVideoData[LoriTuberPendingVideos.vibe1]
                                it[LoriTuberVideos.vibe2] = pendingVideoData[LoriTuberPendingVideos.vibe2]
                                it[LoriTuberVideos.vibe3] = pendingVideoData[LoriTuberPendingVideos.vibe3]
                                it[LoriTuberVideos.vibe4] = pendingVideoData[LoriTuberPendingVideos.vibe4]
                                it[LoriTuberVideos.vibe5] = pendingVideoData[LoriTuberPendingVideos.vibe5]
                                it[LoriTuberVideos.vibe6] = pendingVideoData[LoriTuberPendingVideos.vibe6]
                                it[LoriTuberVideos.vibe7] = pendingVideoData[LoriTuberPendingVideos.vibe7]
                            }

                            LoriTuberPendingVideos.deleteWhere {
                                LoriTuberPendingVideos.id eq pendingVideoId
                            }
                        } else {
                            // TODO: Requery the pending video and check if the stage matches the currently "cached" stage
                            LoriTuberPendingVideos.update({
                                LoriTuberPendingVideos.id eq pendingVideoId
                            }) {
                                it[LoriTuberPendingVideos.currentStage] = newStage
                                it[LoriTuberPendingVideos.currentStageProgressTicks] = 0
                            }
                        }
                    }

                    command.switchScreen(
                        ViewPendingVideoScreen(
                            command,
                            user,
                            it.deferEdit(),
                            character,
                            channelId,
                            pendingVideoId
                        )
                    )
                }

                val viewMotivesButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Voltar ao Vídeo",
                    {
                        emoji = Emoji.fromUnicode("\uD83C\uDFE0")
                    }
                ) {
                    command.switchScreen(
                        ViewPendingVideoScreen(
                            command,
                            user,
                            it.deferEdit(),
                            character,
                            channelId,
                            pendingVideoId
                        )
                    )
                }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Avançar para o Próximo Estágio"

                            description = buildString {
                                appendLine("Ao avançar para o próximo estágio do vídeo (atual: ${result.pendingVideo.currentStage}; novo: $newStage), você não poderá voltar ao estágio anterior!")
                                appendLine("Na verdade você pode, mas irá resetar todo o seu progresso!")
                            }
                        }

                        actionRow(switchStageButton, viewMotivesButton)
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