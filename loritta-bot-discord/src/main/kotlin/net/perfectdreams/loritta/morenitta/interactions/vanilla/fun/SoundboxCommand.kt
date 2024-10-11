package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import io.ktor.client.plugins.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.voice.LorittaVoiceConnection
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import java.awt.Color
import java.util.*
import kotlin.concurrent.thread

class SoundboxCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper  {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Soundbox
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN, UUID.fromString("c6f1a663-185c-431c-8728-ff4cd38326ef")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.VOICE_MOVE_OTHERS)

        subcommand(I18N_PREFIX.Falatron.Label, I18N_PREFIX.Falatron.Description, UUID.fromString("4070f933-9998-42b3-84d1-12a1d7a0e06e")) {
            executor = FalatronExecutor(loritta)
        }
    }

    class FalatronExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val voice = string("voice", SoundboxCommand.I18N_PREFIX.Falatron.Options.Voice.Text) {
                autocomplete { context ->
                    val value = context.event.focusedOption.value

                    // Wait until we have a non-empty model list
                    val models = loritta.falatronModelsManager.models.filter { it.isNotEmpty() }
                        .first()

                    // Then we filter only the models that starts with the user input!
                    models
                        .sortedBy { it.name }
                        .filter {
                            it.name.startsWith(value, true)
                        }.take(DiscordResourceLimits.Command.Options.ChoicesCount).associate {
                            "${it.name} (${it.category})" to it.name
                        }
                }
            }

            val text = string("text", SoundboxCommand.I18N_PREFIX.Falatron.Options.Text.Text) {
                // TODO: Change the allowedLength to 5..300 when Discord fixes a bug where the autocomplete doesn't work if any of the options aren't fulfilling their requirements
                // TODO: Reenable this! Disabled when we migrated to InteraKTions Unleashed because I haven't implemented support for it yet
                // allowedLength = 0..300
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val guildId = context.guildId!!
            val channelId = context.channel.idLong

            context.reply(false) {
                styled(
                    "Isto é uma funcionalidade super hiper mega ultra experimental e ela pode *explodir* a qualquer momento! Ela ainda não está pronta e será melhorada com o passar do tempo... ou talvez até mesmo removida! ${Emotes.LoriSob}",
                    Emotes.LoriMegaphone
                )
            }

            // TODO: Reenable de defer after we remove the warning above
            // context.deferChannelMessage()

            val userConnectedVoiceChannel = loritta.cache.getUserConnectedVoiceChannel(guildId, context.user.idLong)

            if (userConnectedVoiceChannel == null) {
                context.reply(false) {
                    // Not in a voice channel
                    content = "Você precisa estar conectado em um canal de voz para usar o Falatron!"
                }
                return
            }

            // Can we talk there?
            if (!userConnectedVoiceChannel.guild.selfMember.hasPermission(userConnectedVoiceChannel, net.dv8tion.jda.api.Permission.VOICE_CONNECT, net.dv8tion.jda.api.Permission.VOICE_SPEAK)) {
                context.reply(false) {
                    // Looks like we can't...
                    content = "Desculpe, mas eu não tenho permissão para falar no canal ${userConnectedVoiceChannel.asMention}>!"
                }
                return
            }

            // Are we already playing something in another channel already?
            val currentlyActiveVoiceConnection = loritta.voiceConnectionsManager.voiceConnections[guildId]

            if (currentlyActiveVoiceConnection != null) {
                if (currentlyActiveVoiceConnection.isPlaying() && currentlyActiveVoiceConnection.channelId.toLong() != userConnectedVoiceChannel.idLong) {
                    context.reply(false) {
                        // We are already playing in another channel!
                        content = "Eu já estou tocando áudio em outro canal! <#${currentlyActiveVoiceConnection.channelId}>"
                    }
                    return
                }
            }

            // Wait until we have a non-empty model list
            val models = loritta.falatronModelsManager.models.filter { it.isNotEmpty() }
                .first()

            // Validate if the modal exists
            val model = models.firstOrNull { it.name == args[options.voice] }
            if (model == null) {
                context.reply(false) {
                    styled(
                        "Voz desconhecida!",
                        Emotes.LoriSob
                    )
                }
                return
            }

            // Notify that we are trying, ok :sobs:
            val message = context.reply(false) {
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
                loritta.voiceConnectionsManager.getOrCreateVoiceConnection(guildId, userConnectedVoiceChannel.idLong)
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
                    userConnectedVoiceChannel.idLong
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
                        thumbnail = model.image

                    footer(
                        buildString {
                            append("Vozes geradas pelo Falatron")
                            if (model.description.isNotBlank()) {
                                append(" • ${model.description}")
                            }
                        },
                        "https://falatron.com/static/images/logo.png"
                    )
                    color = Color(0, 207, 255).rgb
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
}