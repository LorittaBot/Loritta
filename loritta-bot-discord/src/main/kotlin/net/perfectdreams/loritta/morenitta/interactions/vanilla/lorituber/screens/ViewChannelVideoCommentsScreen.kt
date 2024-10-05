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
                                        
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe1AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de coisas sérias")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe1AlignmentRight0 ->  {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de coisas engraçadas haha funny")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe2AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos didáticos")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe2AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais divertidos")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe3AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos realistas")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe3AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais fofinhos")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe4AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos familiares")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe4AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais excêntricos que inventam muita moda nova faz o L")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe5AlignmentLeft0 ->  {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais tranquilos para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe5AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais agitados iguais do MrBeast")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe6AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos mais seguros que não tem opiniões fortes")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoCorrectVibe6AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, eu gosto de vídeos polêmicos com opiniões polêmicas e fortes")
                                        }

                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe1AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais engraçada")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe1AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais séria")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe2AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais para o entreterimento para que eu não precise pensar muito")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe2AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais didática para aprender coisas novas")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe3AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro algo mais fantasioso")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe3AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu prefiro de vídeos mais realistas, pé no chão, e não essa coisinha fofa aí")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe4AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo que inventa novas modas")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe4AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo mais familiar")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe5AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo mais tchan e agitado")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe5AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo mais calmo para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe6AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo mais polêmico")
                                        }
                                        LoriTuberVideoCommentType.LikedVideoIncorrectVibe6AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu gostei do vídeo, mas eu queria algo mais seguro")
                                        }


                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe1AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de coisas sérias")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe1AlignmentRight0 ->  {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de coisas engraçadas haha funny")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe2AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos didáticos")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe2AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais divertidos")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe3AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos realistas")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe3AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais fofinhos")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe4AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos familiares")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe4AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais excêntricos que inventam muita moda nova faz o L")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe5AlignmentLeft0 ->  {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais tranquilos para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe5AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais agitados iguais do MrBeast")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe6AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos mais seguros que não tem opiniões fortes")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoCorrectVibe6AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, eu gosto de vídeos polêmicos com opiniões polêmicas e fortes")
                                        }

                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe1AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais engraçada")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe1AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais séria")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe2AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais para o entreterimento para que eu não precise pensar muito")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe2AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro de vídeos com uma vibe mais didática para aprender coisas novas")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe3AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro algo mais fantasioso")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe3AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu prefiro de vídeos mais realistas, pé no chão, e não essa coisinha fofa aí")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe4AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo que inventa novas modas")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe4AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo mais familiar")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe5AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo mais tchan e agitado")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe5AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo mais calmo para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe6AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo mais polêmico")
                                        }
                                        LoriTuberVideoCommentType.DislikedVideoIncorrectVibe6AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** Eu não gostei do vídeo, mas eu queria algo mais seguro")
                                        }


                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe1AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de coisas sérias")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe1AlignmentRight0 ->  {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de coisas engraçadas haha funny")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe2AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos didáticos")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe2AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais divertidos")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe3AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos realistas")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe3AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais fofinhos")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe4AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos familiares")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe4AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais excêntricos que inventam muita moda nova faz o L")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe5AlignmentLeft0 ->  {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais tranquilos para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe5AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais agitados iguais do MrBeast")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe6AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos mais seguros que não tem opiniões fortes")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoCorrectVibe6AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu gosto de vídeos polêmicos com opiniões polêmicas e fortes")
                                        }

                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe1AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro de vídeos com uma vibe mais engraçada")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe1AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro de vídeos com uma vibe mais séria")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe2AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro de vídeos com uma vibe mais para o entreterimento para que eu não precise pensar muito")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe2AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro de vídeos com uma vibe mais didática para aprender coisas novas")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe3AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro algo mais fantasioso")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe3AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu prefiro vídeos mais realistas, pé no chão, e não essa coisinha fofa aí")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe4AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu queria algo que inventa novas modas")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe4AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu queria algo mais familiar")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe5AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu queria algo mais tchan e agitado")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe5AlignmentRight0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu queria algo mais calmo para eu ver enquanto eu como Cereal do Gessy™ com Iogurte de Morango")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe6AlignmentLeft0 -> {
                                            appendLine("**${comment.viewerHandle}:** O vídeo é normal, eu queria algo mais polêmico")
                                        }
                                        LoriTuberVideoCommentType.NeutralVideoIncorrectVibe6AlignmentRight0 -> {
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