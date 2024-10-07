package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.lorituber.LoriTuberPendingVideo
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentVibes
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelVideosRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelVideosResponse
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens.CreateVideoVibesScreen.Companion.VIBES_WRAPPER
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import java.util.*

class ViewChannelVideosScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: UUID,
    val videoId: UUID?
) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        if (command.checkMail(user, hook, character, this))
            return

        val result = sendLoriTuberRPCRequestNew<GetChannelVideosResponse>(GetChannelVideosRequest(channelId))

        when (result) {
            is GetChannelVideosResponse.Success -> {
                val updateButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Atualizar",
                    {
                        emoji = Emoji.fromUnicode("\uD83C\uDFE0")
                    }
                ) {
                    command.switchScreen(
                        ViewChannelVideosScreen(
                            command,
                            user,
                            it.deferEdit(),
                            character,
                            channelId,
                            videoId
                        )
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

                val matchedVideo = result.pendingVideo.firstOrNull { it.id == videoId }

                hook.editOriginal(
                    MessageEdit {
                        if (matchedVideo != null) {
                            embed {
                                title = matchedVideo.title

                                description = buildString {
                                    appendLine("```")
                                    var idx = 0
                                    val longestToneLeft = CreateVideoVibesScreen.VIBES_WRAPPER.map { it.value.toneLeft }.maxOf { it.length }
                                    val longestToneRight = CreateVideoVibesScreen.VIBES_WRAPPER.map { it.value.toneRight }.maxOf { it.length }
                                    for (vibe in VIBES_WRAPPER) {
                                        append("${idx + 1}. ")
                                        val vibeValue = matchedVideo.vibes.vibeType(vibe.key)
                                        if (vibeValue == false)
                                            append("✓")
                                        else
                                            append(" ")
                                        append(" ")
                                        append("${vibe.value.toneLeft.padEnd(longestToneLeft, ' ')} ${vibe.value.toneRight.padStart(longestToneRight, ' ')} ")
                                        if (vibeValue == true)
                                            append("✓")
                                        else
                                            append(" ")
                                        appendLine()
                                        idx++
                                    }
                                    appendLine("```")
                                }

                                field("Visualizações", matchedVideo.views.toString())
                                field("Likes", matchedVideo.likes.toString())
                                field("Dislikes", matchedVideo.dislikes.toString())
                                field("Matched Vibes", "${matchedVideo.matchedVibes}/${LoriTuberVideoContentVibes.entries.size}")
                            }
                        } else {
                            embed {
                                title = "Vídeos"
                            }
                        }

                        if (matchedVideo != null) {
                            val unleashedButton = UnleashedButton.of(
                                ButtonStyle.PRIMARY,
                                "Comentários (${matchedVideo.comments.size})",
                                Emoji.fromUnicode("\uD83D\uDCAC")
                            )
                            if (matchedVideo.comments.isNotEmpty()) {
                                actionRow(
                                    loritta.interactivityManager.buttonForUser(
                                        user,
                                        unleashedButton
                                    ) { context ->
                                        command.switchScreen(
                                            ViewChannelVideoCommentsScreen(
                                                command,
                                                user,
                                                context.deferEdit(),
                                                character,
                                                channelId,
                                                matchedVideo.id
                                            )
                                        )
                                    }
                                )
                            } else {
                                actionRow(unleashedButton.asDisabled())
                            }
                        }

                        actionRow(
                            loritta.interactivityManager.stringSelectMenuForUser(
                                user,
                                {
                                    for (video in result.pendingVideo.sortedByDescending { it.postedAtTicks }.take(25)) {
                                        addOption(video.title, video.id.toString())
                                    }

                                    setDefaultValues(videoId.toString())
                                }
                            ) { context, values ->
                                command.switchScreen(
                                    ViewChannelVideosScreen(
                                        command,
                                        user,
                                        context.deferEdit(),
                                        character,
                                        channelId,
                                        UUID.fromString(values.first())
                                    )
                                )
                            }
                        )

                        actionRow(updateButton, viewMotivesButton)
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