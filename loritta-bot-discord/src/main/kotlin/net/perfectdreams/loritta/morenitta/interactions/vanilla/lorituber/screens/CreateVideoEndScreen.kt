package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.ServerInfo
import net.perfectdreams.loritta.lorituber.rpc.packets.CreatePendingVideoRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.CreatePendingVideoResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelByIdRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelByIdResponse
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import java.util.*

class CreateVideoEndScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: UUID,
    private val contentCategory: LoriTuberVideoContentCategory,
    private val contentVibes: LoriTuberVibes
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
                val createVideoButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Criar"
                ) {
                    val hook = it.deferEdit()

                    val result2 = sendLoriTuberRPCRequestNew<CreatePendingVideoResponse>(
                        CreatePendingVideoRequest(
                            channelId,
                            contentCategory,
                            contentVibes
                        )
                    )

                    /* val result2 = loritta.transaction {
                        if (LoriTuberPendingVideos.selectAll().where { LoriTuberPendingVideos.owner eq channelId }.count() != 0L) {
                            return@transaction CreatePendingVideoResult.CharacterIsAlreadyDoingAnotherVideo
                        }

                        val pendingVideoId = LoriTuberPendingVideos.insertAndGetId {
                            it[LoriTuberPendingVideos.owner] = character.id
                            it[LoriTuberPendingVideos.channel] = channelId
                            it[LoriTuberPendingVideos.contentCategory] = this@CreateVideoEndScreen.contentCategory
                            it[LoriTuberPendingVideos.contentLength] = this@CreateVideoEndScreen.contentLength

                            it[LoriTuberPendingVideos.currentStage] = LoriTuberVideoStage.RECORDING
                            it[LoriTuberPendingVideos.currentStageProgressTicks] = 0

                            it[LoriTuberPendingVideos.vibe1] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE1, -1)
                            it[LoriTuberPendingVideos.vibe2] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE2, -1)
                            it[LoriTuberPendingVideos.vibe3] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE3, -1)
                            it[LoriTuberPendingVideos.vibe4] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE4, -1)
                            it[LoriTuberPendingVideos.vibe5] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE5, -1)
                            it[LoriTuberPendingVideos.vibe6] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE6, -1)
                            it[LoriTuberPendingVideos.vibe7] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE7, -1)
                        }

                        LoriTuberCharacters.update({ LoriTuberCharacters.id eq character.id }) {
                            it[LoriTuberCharacters.currentTask] = Json.encodeToString<LoriTuberTask>(LoriTuberTask.WorkingOnVideo(channelId, pendingVideoId.value))
                        }

                        // Then attempt to start the video task
                        return@transaction CreatePendingVideoResult.Success
                    } */

                    when (result2) {
                        CreatePendingVideoResponse.CharacterIsAlreadyDoingAnotherVideo -> {
                            hook.editOriginal(
                                MessageEdit {
                                    content = "Você já tem um vídeo pendente sendo feito!"
                                }
                            ).setReplace(true).await()
                            return@buttonForUser
                        }
                        CreatePendingVideoResponse.UnknownChannel -> {
                            hook.editOriginal(
                                MessageEdit {
                                    content = "Canal desconhecido"
                                }
                            ).setReplace(true).await()
                            return@buttonForUser
                        }
                        CreatePendingVideoResponse.Success -> {
                            command.switchScreen(
                                ViewMotivesScreen(
                                    command,
                                    user,
                                    hook,
                                    LoriTuberCommand.PlayerCharacter(character.id, character.name, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0)
                                )
                            )
                        }
                    }
                }

                val createDebugVideoButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.SECONDARY,
                    "Criar Vídeo Pronto (Debugging)",
                ) { context ->
                    context.deferChannelMessage(true)

                    /* val id = loritta.transaction {
                        val serverInfo = loritta.transaction {
                            LoriTuberServerInfos.selectAll()
                                .where { LoriTuberServerInfos.type eq LoriTuberServer.GENERAL_INFO_KEY }
                                .first()
                                .get(LoriTuberServerInfos.data)
                                .let { Json.decodeFromString<ServerInfo>(it) }
                        }

                        LoriTuberVideos.insertAndGetId {
                            it[LoriTuberVideos.owner] = character.id
                            it[LoriTuberVideos.channel] = channelId
                            it[LoriTuberVideos.public] = true
                            it[LoriTuberVideos.contentCategory] = this@CreateVideoEndScreen.contentCategory
                            it[LoriTuberVideos.contentLength] = this@CreateVideoEndScreen.contentLength
                            // it[LoriTuberVideos.scriptScore] = 10
                            it[LoriTuberVideos.recordingScore] = 10
                            it[LoriTuberVideos.editingScore] = 10
                            it[LoriTuberVideos.thumbnailScore] = 10
                            // it[LoriTuberVideos.titleScore] = 10
                            it[LoriTuberVideos.postedAtTicks] = serverInfo.currentTick
                            // TODO: Actually implement it as a bitset
                            it[LoriTuberVideos.vibes] = 0

                            // TODO: Remove this
                            it[LoriTuberVideos.vibe1] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE1, -1)
                            it[LoriTuberVideos.vibe2] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE2, -1)
                            it[LoriTuberVideos.vibe3] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE3, -1)
                            it[LoriTuberVideos.vibe4] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE4, -1)
                            it[LoriTuberVideos.vibe5] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE5, -1)
                            it[LoriTuberVideos.vibe6] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE6, -1)
                            it[LoriTuberVideos.vibe7] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE7, -1)

                            // wait a bit before we ACTUALLY start giving engagement
                            it[LoriTuberVideos.simulateEngagement] = true

                            // In real life, most YouTube videos seems to have a big boom in the first 7 days, and then it is just a very slow trickle of views
                            // So what we do is have a big boom at first, and then we switch to a linear approach
                            it[LoriTuberVideos.engagementSimulations] = Json.encodeToString<List<LoriTuberEngagementType>>(
                                listOf(
                                    LoriTuberEngagementType.EaseOutQuint(
                                        serverInfo.currentTick + 20,
                                        serverInfo.currentTick + 20 + (1_440 * 7),
                                        0,
                                        0,
                                        0,
                                        1_000,
                                        35,
                                        10
                                    )
                                )
                            )
                            it[LoriTuberVideos.views] = 0
                            it[LoriTuberVideos.likes] = 0
                            it[LoriTuberVideos.dislikes] = 0
                        }
                    }

                    context.reply(true) {
                        styled(
                            "Vídeo de teste criado ID: $id"
                        )
                    } */
                }

                /* val createDebugVideoButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.SECONDARY,
                    "Criar Vídeo Pronto (Debugging)",
                ) { context ->
                    context.deferChannelMessage(true)

                    val id = loritta.transaction {
                        val serverInfo = loritta.transaction {
                            LoriTuberServerInfos.selectAll()
                                .where { LoriTuberServerInfos.type eq LoriTuberServer.GENERAL_INFO_KEY }
                                .first()
                                .get(LoriTuberServerInfos.data)
                                .let { Json.decodeFromString<ServerInfo>(it) }
                        }

                        LoriTuberVideos.insertAndGetId {
                            it[LoriTuberVideos.owner] = character.id
                            it[LoriTuberVideos.channel] = channelId
                            it[LoriTuberVideos.public] = true
                            it[LoriTuberVideos.contentCategory] = this@CreateVideoEndScreen.contentCategory
                            it[LoriTuberVideos.contentLength] = this@CreateVideoEndScreen.contentLength
                            // it[LoriTuberVideos.scriptScore] = 10
                            it[LoriTuberVideos.recordingScore] = 10
                            it[LoriTuberVideos.editingScore] = 10
                            it[LoriTuberVideos.thumbnailScore] = 10
                            // it[LoriTuberVideos.titleScore] = 10
                            it[LoriTuberVideos.postedAtTicks] = serverInfo.currentTick
                            // TODO: Actually implement it as a bitset
                            it[LoriTuberVideos.vibes] = 0

                            // TODO: Remove this
                            it[LoriTuberVideos.vibe1] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE1, -1)
                            it[LoriTuberVideos.vibe2] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE2, -1)
                            it[LoriTuberVideos.vibe3] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE3, -1)
                            it[LoriTuberVideos.vibe4] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE4, -1)
                            it[LoriTuberVideos.vibe5] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE5, -1)
                            it[LoriTuberVideos.vibe6] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE6, -1)
                            it[LoriTuberVideos.vibe7] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE7, -1)

                            // wait a bit before we ACTUALLY start giving engagement
                            it[LoriTuberVideos.simulateEngagement] = true

                            // In real life, most YouTube videos seems to have a big boom in the first 7 days, and then it is just a very slow trickle of views
                            // So what we do is have a big boom at first, and then we switch to a linear approach
                            it[LoriTuberVideos.engagementSimulations] = Json.encodeToString<List<LoriTuberEngagementType>>(
                                listOf(
                                    LoriTuberEngagementType.EaseOutQuint(
                                        serverInfo.currentTick + 20,
                                        serverInfo.currentTick + 20 + (1_440 * 7),
                                        0,
                                        0,
                                        0,
                                        1_000,
                                        35,
                                        10
                                    )
                                )
                            )
                            it[LoriTuberVideos.views] = 0
                            it[LoriTuberVideos.likes] = 0
                            it[LoriTuberVideos.dislikes] = 0
                        }
                    }

                    context.reply(true) {
                        styled(
                            "Vídeo de teste criado ID: $id"
                        )
                    }
                }

                val createDebugVideo10kButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.SECONDARY,
                    "Criar Vídeo Pronto (Debugging / 10k)",
                ) { context ->
                    context.deferChannelMessage(true)

                    fun easeOutQuint(x: Double): Double {
                        return 1 - Math.pow(1 - x, 5.0)
                    }

                    loritta.transaction {
                        val serverInfo = loritta.transaction {
                            LoriTuberServerInfos.selectAll()
                                .where { LoriTuberServerInfos.type eq LoriTuberServer.GENERAL_INFO_KEY }
                                .first()
                                .get(LoriTuberServerInfos.data)
                                .let { Json.decodeFromString<ServerInfo>(it) }
                        }

                        repeat(10_000) {
                            val videoId = LoriTuberVideos.insertAndGetId {
                                it[LoriTuberVideos.owner] = character.id
                                it[LoriTuberVideos.channel] = channelId
                                it[LoriTuberVideos.public] = true
                                it[LoriTuberVideos.contentCategory] = this@CreateVideoEndScreen.contentCategory
                                it[LoriTuberVideos.contentLength] = this@CreateVideoEndScreen.contentLength
                                // it[LoriTuberVideos.scriptScore] = 10
                                it[LoriTuberVideos.recordingScore] = 10
                                it[LoriTuberVideos.editingScore] = 10
                                it[LoriTuberVideos.thumbnailScore] = 10
                                // it[LoriTuberVideos.titleScore] = 10
                                it[LoriTuberVideos.postedAtTicks] = serverInfo.currentTick
                                // TODO: Actually implement it as a bitset
                                it[LoriTuberVideos.vibes] = 0

                                // TODO: Remove this
                                it[LoriTuberVideos.vibe1] =
                                    contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE1, -1)
                                it[LoriTuberVideos.vibe2] =
                                    contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE2, -1)
                                it[LoriTuberVideos.vibe3] =
                                    contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE3, -1)
                                it[LoriTuberVideos.vibe4] =
                                    contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE4, -1)
                                it[LoriTuberVideos.vibe5] =
                                    contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE5, -1)
                                it[LoriTuberVideos.vibe6] =
                                    contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE6, -1)
                                it[LoriTuberVideos.vibe7] =
                                    contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE7, -1)

                                // wait a bit before we ACTUALLY start giving engagement
                                it[LoriTuberVideos.simulateEngagement] = true

                                // In real life, most YouTube videos seems to have a big boom in the first 7 days, and then it is just a very slow trickle of views
                                // So what we do is have a big boom at first, and then we switch to a linear approach
                                it[LoriTuberVideos.engagementSimulations] =
                                    Json.encodeToString<List<LoriTuberEngagementType>>(
                                        listOf(
                                            LoriTuberEngagementType.EaseOutQuint(
                                                serverInfo.currentTick + 20,
                                                serverInfo.currentTick + 20 + (1_440 * 7),
                                                0,
                                                0,
                                                0,
                                                1_000,
                                                35,
                                                10
                                            )
                                        )
                                    )
                                it[LoriTuberVideos.views] = 0
                                it[LoriTuberVideos.likes] = 0
                                it[LoriTuberVideos.dislikes] = 0
                            }

                            LoriTuberVideoSimulatedEngagements.insert {
                                it[LoriTuberVideoSimulatedEngagements.video] = videoId
                                it[LoriTuberVideoSimulatedEngagements.easingType] = "easeOutQuint"
                                it[LoriTuberVideoSimulatedEngagements.engagementStartTick] = serverInfo.currentTick
                                it[LoriTuberVideoSimulatedEngagements.engagementEndTick] = serverInfo.currentTick + (1_440 * 7)
                                it[LoriTuberVideoSimulatedEngagements.startViews] = 0
                                it[LoriTuberVideoSimulatedEngagements.startLikes] = 0
                                it[LoriTuberVideoSimulatedEngagements.startDislikes] = 0
                                it[LoriTuberVideoSimulatedEngagements.targetViews] = 1_000
                                it[LoriTuberVideoSimulatedEngagements.targetLikes] = 35
                                it[LoriTuberVideoSimulatedEngagements.targetDislikes] = 10
                            }
                        }
                    }

                    context.reply(true) {
                        styled(
                            "Vídeos de teste criados"
                        )
                    }
                } */

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Criação de Vídeos [4/4]"

                            description = """Começar a criar o vídeo?""".trimMargin()
                        }

                        actionRow(createVideoButton, /* createDebugVideoButton, createDebugVideo10kButton */)
                    }
                ).setReplace(true).await()
            }
        }
    }

    private fun l(serverInfo: ServerInfo) = serverInfo.currentTick

    sealed class CreateVideoEndResult {
        data object UnknownChannel : CreateVideoEndResult()
        data class Channel(val channel: LoriTuberChannel) : CreateVideoEndResult()
    }

    sealed class CreatePendingVideoResult {
        data object CharacterIsAlreadyDoingAnotherVideo : CreatePendingVideoResult()
        data object Success : CreatePendingVideoResult()
    }
}