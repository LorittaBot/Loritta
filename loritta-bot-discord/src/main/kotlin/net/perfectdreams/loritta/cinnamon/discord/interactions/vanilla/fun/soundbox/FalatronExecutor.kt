package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.soundbox

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import io.ktor.client.plugins.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.entities.messages.editMessage
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.SoundboxCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.falatron.FalatronModelsManager
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.discord.voice.LorittaVoiceConnection
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import kotlin.concurrent.thread

class FalatronExecutor(loritta: LorittaBot, private val falatronModelsManager: FalatronModelsManager) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val voice = string("voice", SoundboxCommand.I18N_PREFIX.Falatron.Options.Voice.Text) {
            cinnamonAutocomplete { _, focused ->
                // Wait until we have a non-empty model list
                val models = falatronModelsManager.models.filter { it.isNotEmpty() }
                    .first()

                // Then we filter only the models that starts with the user input!
                models
                    .sortedBy { it.name }
                    .filter {
                        it.name.startsWith(focused.value, true)
                    }.take(25).associate {
                        "${it.name} (${it.category})" to it.name
                    }
            }
        }

        val text = string("text", SoundboxCommand.I18N_PREFIX.Falatron.Options.Text.Text) {
            // TODO: Change the allowedLength to 5..300 when Discord fixes a bug where the autocomplete doesn't work if any of the options aren't fulfilling their requirements
            allowedLength = 0..300
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext) // Only guilds
            return

        val guildId = context.guildId
        val channelId = context.channelId

        context.sendMessage {
            styled(
                "Isto é uma funcionalidade super hiper mega ultra experimental e ela pode *explodir* a qualquer momento! Ela ainda não está pronta e será melhorada com o passar do tempo... ou talvez até mesmo removida! ${Emotes.LoriSob}",
                Emotes.LoriMegaphone
            )
        }

        // TODO: Reenable de defer after we remove the warning above
        // context.deferChannelMessage()

        val userConnectedVoiceChannel = loritta.cache.getUserConnectedVoiceChannel(guildId, context.user.id) ?: context.fail {
            // Not in a voice channel
            content = "Você precisa estar conectado em um canal de voz para usar o Falatron!"
        }

        // Can we talk there?
        if (!userConnectedVoiceChannel.guild.selfMember.hasPermission(userConnectedVoiceChannel, net.dv8tion.jda.api.Permission.VOICE_CONNECT, net.dv8tion.jda.api.Permission.VOICE_SPEAK))
            context.fail {
                // Looks like we can't...
                content = "Desculpe, mas eu não tenho permissão para falar no canal ${userConnectedVoiceChannel.asMention}>!"
            }

        // Are we already playing something in another channel already?
        val currentlyActiveVoiceConnection = loritta.voiceConnectionsManager.voiceConnections[guildId]

        if (currentlyActiveVoiceConnection != null) {
            if (currentlyActiveVoiceConnection.isPlaying() && currentlyActiveVoiceConnection.channelId.toLong() != userConnectedVoiceChannel.idLong)
                context.fail {
                    // We are already playing in another channel!
                    content = "Eu já estou tocando áudio em outro canal! <#${currentlyActiveVoiceConnection.channelId}>"
                }
        }

        // Wait until we have a non-empty model list
        val models = falatronModelsManager.models.filter { it.isNotEmpty() }
            .first()

        // Validate if the modal exists
        val model = models.firstOrNull { it.name == args[options.voice] }
            ?: context.fail {
                styled(
                    "Voz desconhecida!",
                    Emotes.LoriSob
                )
            }

        // Notify that we are trying, ok :sobs:
        val message = context.sendMessage {
            styled(
                "Pedindo para o Falatron gerar as vozes...",
                Emotes.LoriLick
            )
        }

        val result = generateTextAsOpusFrames(
            args[options.voice],
            args[options.text]
        ) {
            // TODO: This is nasty, don't use GlobalScope (we should really create a launch/async func in the CommandContext)
            GlobalScope.launch {
                message.editMessage {
                    styled(
                        "Pedindo para o Falatron gerar as vozes... Posição na fila: $it",
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

        // Let's create a voice connection!
        val lorittaVoiceConnection = try {
            loritta.voiceConnectionsManager.getOrCreateVoiceConnection(guildId, Snowflake(userConnectedVoiceChannel.idLong))
        } catch (e: Exception) {
            // Welp, something went wrong
            message.editMessage {
                styled(
                    "Eu não consegui entrar no canal de voz... Se eu estou conectada no canal de voz, tente me desconectar do canal e use o comando novamente!",
                    Emotes.LoriSob
                )
            }
            return
        }

        // Now let's queue the audio clip!
        lorittaVoiceConnection.queue(
            LorittaVoiceConnection.AudioClipInfo(
                opusFrames,
                Snowflake(userConnectedVoiceChannel.idLong)
            )
        )

        message.editMessage {
            styled(
                "Voz gerada com sucesso! Tocando em ${userConnectedVoiceChannel.asMention}",
                Emotes.LoriHi
            )

            embed {
                if (model.author != null)
                    author(model.author)
                url = "https://falatron.com/"

                title = model.name
                description = "*${args[options.text]}*"

                if (model.image.isNotBlank())
                    thumbnailUrl = model.image

                footer(
                    buildString {
                        append("Vozes geradas pelo Falatron")
                        if (model.description.isNotBlank()) {
                            append(" • ${model.description}")
                        }
                    },
                    "https://falatron.com/static/images/logo.png"
                )
                color = Color(0, 207, 255)
            }
        }
    }

    private suspend fun generateTextAsOpusFrames(
        voice: String,
        text: String,
        queuePositionCallback: (Int) -> (Unit)
    ): FalatronVoiceResult {
        // First: Request Falatron voice
        val generatedAudioInMP3Format = try {
            loritta.falatron.generate(
                voice,
                text,
                queuePositionCallback
            )
        } catch (e: Exception) {
            if (e is IllegalStateException || e is HttpRequestTimeoutException) {
                e.printStackTrace()

                // We tried ok
                return FalatronVoiceResult.FalatronOffline
            }
            throw e
        }

        // Second: Convert the audio
        val generatedAudioInOGGFormat = convertAudio(generatedAudioInMP3Format)

        // Third: Load the OGG data from the generated audio and extract the frames
        return FalatronVoiceResult.Success(loritta.soundboard.extractOpusFrames(generatedAudioInOGGFormat))
    }

    private fun convertAudio(byteArray: ByteArray): ByteArray {
        val processBuilder = ProcessBuilder(
            loritta.config.loritta.binaries.ffmpeg,
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
        // We can't use "readAllBytes" because ffmpeg stops writing to the InputStream until we read more things from it
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
        object FalatronOffline : FalatronVoiceResult()
        class Success(val opusFrames: List<ByteArray>) : FalatronVoiceResult()
    }
}