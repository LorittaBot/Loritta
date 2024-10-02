package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.lorituber.LoriTuberPendingVideo
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelByIdRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelByIdResponse
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel

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

        val result = sendLoriTuberRPCRequestNew<GetChannelByIdResponse>(GetChannelByIdRequest(channelId))

        /* val result = loritta.transaction {
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
        } */

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
                val createOrViewPendingVideoButton = if (result.channel.pendingVideos.isNotEmpty()) {
                    val pendingVideo = result.channel.pendingVideos.first()

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
                        "Criar Vídeo",
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
                    "Ver Vídeos",
                    {
                        emoji = Emoji.fromUnicode("\uD83C\uDFAC")
                    }
                ) { context ->
                    val hook = context.deferEdit()

                    command.switchScreen(
                        ViewChannelVideosScreen(
                            command,
                            user,
                            hook,
                            character,
                            channelId,
                            null
                        )
                    )
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

                            field("Inscritos", result.channel.subscribers.toString())

                            for (channelRelationship in result.channel.channelRelationships) {
                                field("[DEBUG] ${channelRelationship.key}", channelRelationship.value.toString())
                            }
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