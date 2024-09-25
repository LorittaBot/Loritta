package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.*
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentVibes
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoStage
import net.perfectdreams.loritta.lorituber.LoriTuberServer
import net.perfectdreams.loritta.lorituber.ServerInfo
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class CreateVideoEndScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: Long,
    private val contentCategory: LoriTuberVideoContentCategory,
    private val contentLength: LoriTuberContentLength,
    private val contentVibes: Map<LoriTuberVideoContentVibes, Int>
) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        val result = loritta.transaction {
            val channel = LoriTuberChannels.selectAll()
                .where {
                    LoriTuberChannels.id eq channelId
                }
                .firstOrNull()

            if (channel == null)
                return@transaction CreateVideoEndResult.UnknownChannel

            return@transaction CreateVideoEndResult.Channel(
                LoriTuberChannel(
                    channel[LoriTuberChannels.id].value,
                    channel[LoriTuberChannels.name]
                )
            )
        }

        when (result) {
            CreateVideoEndResult.UnknownChannel -> {
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
            is CreateVideoEndResult.Channel -> {
                val createVideoButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Criar"
                ) {
                    val hook = it.deferEdit()

                    val result2 = loritta.transaction {
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
                    }

                    when (result2) {
                        CreatePendingVideoResult.CharacterIsAlreadyDoingAnotherVideo -> {
                            it.deferEdit().editOriginal(
                                MessageEdit {
                                    content = "Você já tem um vídeo pendente sendo feito!"
                                }
                            ).setReplace(true).await()
                            return@buttonForUser
                        }
                        CreatePendingVideoResult.Success -> {
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
                            it[LoriTuberVideos.contentCategory] = this@CreateVideoEndScreen.contentCategory
                            it[LoriTuberVideos.contentLength] = this@CreateVideoEndScreen.contentLength
                            // it[LoriTuberVideos.scriptScore] = 10
                            it[LoriTuberVideos.recordingScore] = 10
                            it[LoriTuberVideos.editingScore] = 10
                            it[LoriTuberVideos.thumbnailScore] = 10
                            // it[LoriTuberVideos.titleScore] = 10
                            it[LoriTuberVideos.postedAtTicks] = serverInfo.currentTick

                            it[LoriTuberPendingVideos.vibe1] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE1, -1)
                            it[LoriTuberPendingVideos.vibe2] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE2, -1)
                            it[LoriTuberPendingVideos.vibe3] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE3, -1)
                            it[LoriTuberPendingVideos.vibe4] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE4, -1)
                            it[LoriTuberPendingVideos.vibe5] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE5, -1)
                            it[LoriTuberPendingVideos.vibe6] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE6, -1)
                            it[LoriTuberPendingVideos.vibe7] = contentVibes.getOrDefault(LoriTuberVideoContentVibes.VIBE7, -1)
                        }
                    }

                    command.switchScreen(
                        ViewMotivesScreen(
                            command,
                            user,
                            hook,
                            LoriTuberCommand.PlayerCharacter(character.id, character.name, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0)
                        )
                    )

                    context.reply(true) {
                        styled(
                            "Vídeo de teste criado ID: $id"
                        )
                    }
                }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Criação de Vídeos [4/4]"

                            description = """Começar a criar o vídeo?""".trimMargin()
                        }

                        actionRow(createVideoButton, createDebugVideoButton)
                    }
                ).setReplace(true).await()
            }
        }
    }

    sealed class CreateVideoEndResult {
        data object UnknownChannel : CreateVideoEndResult()
        data class Channel(val channel: LoriTuberChannel) : CreateVideoEndResult()
    }

    sealed class CreatePendingVideoResult {
        data object CharacterIsAlreadyDoingAnotherVideo : CreatePendingVideoResult()
        data object Success : CreatePendingVideoResult()
    }
}