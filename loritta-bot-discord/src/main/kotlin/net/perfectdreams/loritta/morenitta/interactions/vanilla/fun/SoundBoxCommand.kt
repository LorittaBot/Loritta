package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import dev.kord.common.Color
import io.ktor.client.plugins.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.soundboard.SoundboardAudio
import net.perfectdreams.loritta.cinnamon.discord.voice.LorittaVoiceConnection
import net.perfectdreams.loritta.cinnamon.discord.voice.LorittaVoiceConnectionManager
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import kotlin.concurrent.thread

class SoundBoxCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Soundbox
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        isGuildOnly = true

        subcommand(I18N_PREFIX.Falatron.Label, I18N_PREFIX.Falatron.Description) {
            executor = FalatronExecutor()
        }
    }

    inner class FalatronExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val voice = string("voice", I18N_PREFIX.Falatron.Options.Voice.Text) {
                autocomplete { context ->
                    val focused = context.event.focusedOption

                    val models = context.loritta.falatronModelsManager.models.filter { it.isNotEmpty() }
                        .first()

                    models
                        .sortedBy { it.name }
                        .filter {
                            it.name.startsWith(focused.value, true)
                        }.take(DiscordResourceLimits.Command.Options.ChoicesCount).associate {
                            "${it.name} (${it.category})" to it.name
                        }
                }
            }

            val text = string("text", I18N_PREFIX.Falatron.Options.Text.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(true) {
                styled(
                    "Isto é uma funcionalidade super hiper mega ultra experimental e ela pode *explodir* a qualquer momento! Ela ainda não está pronta e será melhorada com o passar do tempo... ou talvez até mesmo removida! ${Emotes.LoriSob}",
                    Emotes.LoriMegaphone
                )
            }

            val userConnectedVoiceChannel = context.loritta.cache.getUserConnectedVoiceChannel(context.guildId!!, context.user.idLong) ?: context.fail(true) {
                content = "Você precisa estar conectado em um canal de voz para usar o Falatron!"
            }

            if (!userConnectedVoiceChannel.guild.selfMember.hasPermission(
                userConnectedVoiceChannel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK
            )) context.fail(true) {
                content = "Desculpe, mas eu não tenho permissão para falar no canal ${userConnectedVoiceChannel.asMention}!"
            }

            val currentlyActiveVoiceConnection = context.loritta.voiceConnectionsManager.voiceConnections[context.guildId]

            if (currentlyActiveVoiceConnection != null) {
                if (currentlyActiveVoiceConnection.isPlaying() && currentlyActiveVoiceConnection.channelId != userConnectedVoiceChannel.idLong) context.fail(true) {
                    content = "Eu já estou tocando áudio em outro canal! <#${currentlyActiveVoiceConnection.channelId}>"
                }
            }

            val models = context.loritta.falatronModelsManager.models.filter { it.isNotEmpty() }
                .first()

            val model = models.firstOrNull { it.name == args[options.voice] }
                ?: context.fail(true) {
                    styled(
                        "Voz desconhecida!",
                        Emotes.LoriSob
                    )
                }

            val message = context.reply(false) {
                styled(
                    "Pedindo para o Falatron gerar as vozes...",
                    Emotes.LoriLick
                )
            }

            val result = generateTextAsOpusFrames(
                context,
                args[options.voice],
                args[options.text]
            ) {
                GlobalScope.launch {
                    message.editMessage {
                        styled(
                            "Pedindo para o Falatron gerar as vozes... (Posição na fila: $it)",
                            Emotes.LoriLick
                        )
                    }
                }
            }

            val opusFrames = when (result) {
                is FalatronVoiceResult.Success -> result.opusFrames
                is FalatronVoiceResult.FalatronOffline -> {
                    message.editMessage {
                        styled(
                            "Parece que o Falatron está instável ou offline... Tente novamente mais tarde!",
                            Emotes.LoriSleeping
                        )
                    }
                    return
                }
            }

            val lorittaVoiceConnection = try {
                context.loritta.voiceConnectionsManager.getOrCreateVoiceConnection(context.guildId!!, userConnectedVoiceChannel.idLong)
            } catch (e: Exception) {
                message.editMessage {
                    styled(
                        "Eu não consegui entrar no canal de voz... Se eu estou conectada no canal de voz, tente me desconectar do canal e use o comando novamente!",
                        Emotes.LoriSob
                    )
                }
                return
            }

            lorittaVoiceConnection.queue(
                LorittaVoiceConnection.AudioClipInfo(
                    opusFrames,
                    userConnectedVoiceChannel.idLong
                )
            )

            message.editMessage {
                styled(
                    "Voz gerada com sucesso! Tocando em ${userConnectedVoiceChannel.asMention}",
                    Emotes.LoriHi
                )

                embed {
                    if (model.author != null) author {
                        name = model.author
                        url = "https://falatron.com/"
                    }

                    title = model.name
                    description = "*${args[options.text]}*"

                    if (model.image.isNotBlank())
                        thumbnail = model.image

                    footer {
                        name = buildString {
                            append("Vozes geradas pelo Falatron")
                            if (model.description.isNotBlank()) {
                                append(" • ${model.description}")
                            }
                        }

                        iconUrl = "https://falatron.com/static/images/logo.png"
                    }

                    color = Color(0, 207, 255).rgb
                }
            }
        }
    }

    private suspend fun generateTextAsOpusFrames(
        context: UnleashedContext,
        voice: String,
        text: String,
        queuePositionCallback: (Int) -> (Unit)
    ): FalatronVoiceResult {
        val generatedAudioInMP3Format = try {
            context.loritta.falatron.generate(
                voice,
                text,
                queuePositionCallback
            )
        } catch (e: Exception) {
            if (e is IllegalStateException || e is HttpRequestTimeoutException) {
                e.printStackTrace()

                return FalatronVoiceResult.FalatronOffline
            }
            throw e
        }

        val generatedAudioInOGGFormat = convertAudio(context, generatedAudioInMP3Format)

        return FalatronVoiceResult.Success(
            context.loritta.soundboard.extractOpusFrames(generatedAudioInOGGFormat)
        )
    }

    private fun convertAudio(context: UnleashedContext, byteArray: ByteArray): ByteArray {
        val processBuilder = ProcessBuilder(
            context.loritta.config.loritta.binaries.ffmpeg,
            // "-hide_banner",
            // "-loglevel",
            // "error",
            "-f",
            "mp3",
            "-i",
            "-", // We will write to output stream
            "-ar",
            "48000",
            "-c:a",
            "libopus",
            "-ac",
            "2",
            "-f",
            "ogg",
            "-"
        ).start()

        val inputStream = processBuilder.inputStream
        val outputStream = processBuilder.outputStream

        val input = mutableListOf<Byte>()

        thread {
            while (true) {
                val value = inputStream.read()
                if (value == -1)
                    break
                input.add(value.toByte())
            }
        }

        outputStream.write(byteArray)
        outputStream.close()

        processBuilder.waitFor()

        return input.toByteArray()
    }

    private sealed class FalatronVoiceResult {
        data object FalatronOffline : FalatronVoiceResult()
        class Success(val opusFrames: List<ByteArray>) : FalatronVoiceResult()
    }
}