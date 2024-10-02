package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.lorituber.LoriTuberPendingVideo
import net.perfectdreams.loritta.lorituber.LoriTuberVideoCommentType
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelVideosRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelVideosResponse
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel

class ViewChannelVideoCommentsScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: Long,
    val videoId: Long
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
                        ViewChannelVideoCommentsScreen(
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

                val matchedVideo = result.pendingVideo.first { it.id == videoId }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Comentários do Vídeo ${matchedVideo.title}"

                            description = buildString {
                                for (comment in matchedVideo.comments) {
                                    when (comment.commentType) {
                                        LoriTuberVideoCommentType.First0 -> {
                                            appendLine("**${comment.viewerHandle}:** First!")
                                        }
                                        
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe1AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de coisas sérias")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe1AligmentRight0 ->  {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de coisas engraçadas haha funny")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe2AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos didáticos")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe2AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais divertidos")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe3AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos realistas")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe3AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais fofinhos")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe4AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos familiares")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe4AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais excêntricos que inventam muita moda nova faz o L")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe5AligmentLeft0 ->  {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais tranquilos para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe5AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais agitados iguais do MrBeast")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe6AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais seguros que não tem opiniões fortes")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe6AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos polêmicos com opiniões polêmicas e fortes")
                                        }

                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe1AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais engraçada")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe1AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais séria")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe2AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais para o entreterimento para que eu não precise pensar muito")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe2AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais didática para aprender coisas novas")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe3AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro algo mais fantasioso")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe3AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro de vídeos mais realistas, pé no chão, e não essa coisinha fofa aí")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe4AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo que inventa novas modas")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe4AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo mais familiar")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe5AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo mais tchan e agitado")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe5AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo mais calmo para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe6AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo mais polêmico")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe6AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo mais seguro")
                                        }


                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe1AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de coisas sérias")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe1AligmentRight0 ->  {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de coisas engraçadas haha funny")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe2AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos didáticos")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe2AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais divertidos")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe3AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos realistas")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe3AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais fofinhos")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe4AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos familiares")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe4AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais excêntricos que inventam muita moda nova faz o L")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe5AligmentLeft0 ->  {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais tranquilos para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe5AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais agitados iguais do MrBeast")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe6AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais seguros que não tem opiniões fortes")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe6AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos polêmicos com opiniões polêmicas e fortes")
                                        }

                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe1AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais engraçada")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe1AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais séria")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe2AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais para o entreterimento para que eu não precise pensar muito")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe2AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais didática para aprender coisas novas")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe3AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro algo mais fantasioso")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe3AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro de vídeos mais realistas, pé no chão, e não essa coisinha fofa aí")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe4AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo que inventa novas modas")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe4AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo mais familiar")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe5AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo mais tchan e agitado")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe5AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo mais calmo para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe6AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo mais polêmico")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe6AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo mais seguro")
                                        }


                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe1AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de coisas sérias")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe1AligmentRight0 ->  {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de coisas engraçadas haha funny")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe2AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos didáticos")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe2AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais divertidos")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe3AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos realistas")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe3AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais fofinhos")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe4AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos familiares")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe4AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais excêntricos que inventam muita moda nova faz o L")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe5AligmentLeft0 ->  {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais tranquilos para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe5AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais agitados iguais do MrBeast")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe6AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais seguros que não tem opiniões fortes")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe6AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos polêmicos com opiniões polêmicas e fortes")
                                        }

                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe1AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro de vídeos com uma vibe mais engraçada")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe1AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro de vídeos com uma vibe mais séria")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe2AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro de vídeos com uma vibe mais para o entreterimento para que eu não precise pensar muito")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe2AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro de vídeos com uma vibe mais didática para aprender coisas novas")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe3AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro algo mais fantasioso")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe3AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro vídeos mais realistas, pé no chão, e não essa coisinha fofa aí")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe4AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu queria algo que inventa novas modas")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe4AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu queria algo mais familiar")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe5AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu queria algo mais tchan e agitado")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe5AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu queria algo mais calmo para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe6AligmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu queria algo mais polêmico")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe6AligmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu queria algo mais seguro")
                                        }
                                    }
                                }
                            }
                        }

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