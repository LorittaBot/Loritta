package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.*
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentVibes
import net.perfectdreams.loritta.lorituber.LoriTuberPendingVideo
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

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

        val result = loritta.transaction {
            val channel = LoriTuberChannels.selectAll()
                .where {
                    LoriTuberChannels.id eq channelId
                }
                .firstOrNull()

            if (channel == null)
                return@transaction ViewChannelResult.UnknownChannel

            val pendingVideos = LoriTuberPendingVideos.select {
                LoriTuberPendingVideos.channel eq channelId
            }.map {
                LoriTuberPendingVideo(
                    it[LoriTuberPendingVideos.id].value,
                    it[LoriTuberPendingVideos.contentCategory],
                    it[LoriTuberPendingVideos.contentLength],
                    it[LoriTuberPendingVideos.currentStage],
                    it[LoriTuberPendingVideos.currentStageProgressTicks],
                    it[LoriTuberPendingVideos.recordingScore],
                    it[LoriTuberPendingVideos.editingScore],
                    it[LoriTuberPendingVideos.thumbnailScore],
                    it[LoriTuberPendingVideos.videoResolution],
                )
            }

            return@transaction ViewChannelResult.Channel(
                LoriTuberChannel(
                    channel[LoriTuberChannels.id].value,
                    channel[LoriTuberChannels.name]
                ),
                pendingVideos
            )
        }

        when (result) {
            ViewChannelResult.UnknownChannel -> {
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
            is ViewChannelResult.Channel -> {
                val createOrViewPendingVideoButton = if (result.pendingVideos.isNotEmpty()) {
                    val pendingVideo = result.pendingVideos.first()

                    loritta.interactivityManager.buttonForUser(
                        user,
                        ButtonStyle.PRIMARY,
                        "Ver Vídeo em Produção",
                        {
                            emoji = Emoji.fromUnicode("\uD83C\uDFAC")
                        }
                    ) { context ->
                        val interactionHook = context.deferEdit()

                        command.switchScreen(
                            ViewPendingVideoScreen(
                                command,
                                user,
                                interactionHook,
                                character,
                                channelId,
                                pendingVideo.id
                            )
                        )
                    }
                } else {
                    loritta.interactivityManager.buttonForUser(
                        user,
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

                val viewLastVideoButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Ver Último Vídeo",
                    {
                        emoji = Emoji.fromUnicode("\uD83C\uDFAC")
                    }
                ) { context ->
                    context.deferChannelMessage(false)

                    // TODO: Refactor this to be a list!
                    data class Result(
                        val videoData: ResultRow,
                        val views: Long,
                        val likes: Long,
                        val dislikes: Long,
                        val vibe1Avg: Double,
                        val vibe2Avg: Double,
                        val vibe3Avg: Double,
                        val vibe4Avg: Double,
                        val vibe5Avg: Double,
                        val vibe6Avg: Double,
                        val vibe7Avg: Double
                    )

                    val result = loritta.transaction {
                        val videoData = LoriTuberVideos.selectAll()
                            .where {
                                LoriTuberVideos.channel eq channelId
                            }
                            .orderBy(LoriTuberVideos.id, SortOrder.DESC)
                            .first()

                        val views = LoriTuberViewerViews.selectAll()
                            .where {
                                LoriTuberViewerViews.video eq videoData[LoriTuberVideos.id]
                            }
                            .count()

                        val likes = LoriTuberViewerLikes.selectAll()
                            .where {
                                LoriTuberViewerLikes.video eq videoData[LoriTuberVideos.id]
                            }
                            .count()

                        val dislikes = LoriTuberViewerDislikes.selectAll()
                            .where {
                                LoriTuberViewerDislikes.video eq videoData[LoriTuberVideos.id]
                            }
                            .count()

                        val likesStuff = LoriTuberViewerLikes.selectAll()
                            .where {
                                LoriTuberViewerLikes.video eq videoData[LoriTuberVideos.id]
                            }
                            .toList()

                        val dislikesStuff = LoriTuberViewerDislikes.selectAll()
                            .where {
                                LoriTuberViewerDislikes.video eq videoData[LoriTuberVideos.id]
                            }
                            .toList()

                        val vibe1Avg = (likesStuff.map { it[LoriTuberViewerLikes.vibe1] } + dislikesStuff.map { it[LoriTuberViewerDislikes.vibe1] }).average()
                        val vibe2Avg = (likesStuff.map { it[LoriTuberViewerLikes.vibe2] } + dislikesStuff.map { it[LoriTuberViewerDislikes.vibe2] }).average()
                        val vibe3Avg = (likesStuff.map { it[LoriTuberViewerLikes.vibe3] } + dislikesStuff.map { it[LoriTuberViewerDislikes.vibe3] }).average()
                        val vibe4Avg = (likesStuff.map { it[LoriTuberViewerLikes.vibe4] } + dislikesStuff.map { it[LoriTuberViewerDislikes.vibe4] }).average()
                        val vibe5Avg = (likesStuff.map { it[LoriTuberViewerLikes.vibe5] } + dislikesStuff.map { it[LoriTuberViewerDislikes.vibe5] }).average()
                        val vibe6Avg = (likesStuff.map { it[LoriTuberViewerLikes.vibe6] } + dislikesStuff.map { it[LoriTuberViewerDislikes.vibe6] }).average()
                        val vibe7Avg = (likesStuff.map { it[LoriTuberViewerLikes.vibe7] } + dislikesStuff.map { it[LoriTuberViewerDislikes.vibe7] }).average()

                        Result(videoData, views, likes, dislikes, vibe1Avg, vibe2Avg, vibe3Avg, vibe4Avg, vibe5Avg, vibe6Avg, vibe7Avg)
                    }

                    context.reply(false) {
                        embed {
                            title = "Vídeo"

                            field("Visualizações", result.views.toString())
                            field("Gostei", result.likes.toString())
                            field("Não Gostei", result.dislikes.toString())

                            field("Vibe 1 do Vídeo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE1]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE1]!!.toneRight})", result.videoData[LoriTuberVideos.vibe1].toString(), false)
                            field("Vibe 2 do Vídeo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE2]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE2]!!.toneRight})", result.videoData[LoriTuberVideos.vibe2].toString())
                            field("Vibe 3 do Vídeo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE3]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE3]!!.toneRight})", result.videoData[LoriTuberVideos.vibe3].toString())
                            field("Vibe 4 do Vídeo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE4]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE4]!!.toneRight})", result.videoData[LoriTuberVideos.vibe4].toString())
                            field("Vibe 5 do Vídeo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE5]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE5]!!.toneRight})", result.videoData[LoriTuberVideos.vibe5].toString())
                            field("Vibe 6 do Vídeo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE6]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE6]!!.toneRight})", result.videoData[LoriTuberVideos.vibe6].toString())
                            field("Vibe 7 do Vídeo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE7]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE7]!!.toneRight})", result.videoData[LoriTuberVideos.vibe7].toString())

                            field("Vibe 1 do Algoritmo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE1]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE1]!!.toneRight}) Avg", result.vibe1Avg.toString(), false)
                            field("Vibe 2 do Algoritmo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE2]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE2]!!.toneRight}) Avg", result.vibe2Avg.toString())
                            field("Vibe 3 do Algoritmo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE3]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE3]!!.toneRight}) Avg", result.vibe3Avg.toString())
                            field("Vibe 4 do Algoritmo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE4]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE4]!!.toneRight}) Avg", result.vibe4Avg.toString())
                            field("Vibe 5 do Algoritmo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE5]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE5]!!.toneRight}) Avg", result.vibe5Avg.toString())
                            field("Vibe 6 do Algoritmo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE6]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE6]!!.toneRight}) Avg", result.vibe6Avg.toString())
                            field("Vibe 7 do Algoritmo (${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE7]!!.toneLeft} x ${CreateVideoVibesScreen.VIBES_WRAPPER[LoriTuberVideoContentVibes.VIBE7]!!.toneRight}) Avg", result.vibe7Avg.toString())
                        }
                    }
                }

                val viewRequests = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Ver Requests",
                    {
                        emoji = Emoji.fromUnicode("\uD83C\uDFAC")
                    }
                ) { context ->
                }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Canal ${result.channel.name}"

                            field("ayaya", "yay!!!")
                        }

                        actionRow(createOrViewPendingVideoButton, viewLastVideoButton, viewRequests)
                        actionRow(viewMotivesButton)
                    }
                ).setReplace(true).await()
            }
        }
    }

    sealed class ViewChannelResult {
        data object UnknownChannel : ViewChannelResult()
        data class Channel(val channel: LoriTuberChannel, val pendingVideos: List<LoriTuberPendingVideo>) : ViewChannelResult()
    }

    sealed class ContinuePendingVideoResult {
        data object MoodTooLow : ContinuePendingVideoResult()
        data object Success : ContinuePendingVideoResult()
    }
}