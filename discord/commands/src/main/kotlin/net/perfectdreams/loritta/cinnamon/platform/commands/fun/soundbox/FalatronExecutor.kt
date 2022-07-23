package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox

import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.entities.messages.editMessage
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.*
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.SoundboxCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.*
import java.util.*
import kotlin.time.Duration.Companion.minutes

class FalatronExecutor(val m: LorittaCinnamon, private val falatronModelsManager: FalatronModelsManager) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val voice = string("voice", SoundboxCommand.I18N_PREFIX.Falatron.Options.Voice.Text)
                .autocomplete(FalatronVoiceAutocompleteExecutor)
                .register()

            val text = string("text", SoundboxCommand.I18N_PREFIX.Falatron.Options.Text.Text)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext) // Only guilds
            return

        try {
            coroutineScope {
                val guildId = context.guildId

                context.deferChannelMessage()

                val userConnectedVoiceChannelId = m.cache.getUserConnectedVoiceChannel(guildId, context.user.id) ?: context.fail {
                    // Not in a voice channel
                    content = "Você precisa estar conectado em um canal de voz para usar o Falatron!"
                }

                // Can we talk there?
                if (!m.cache.lorittaHasPermission(context.guildId, userConnectedVoiceChannelId, Permission.Connect, Permission.Speak))
                    context.fail {
                        // Looks like we can't...
                        content = "Desculpe, mas eu não tenho permissão para falar no canal <#${userConnectedVoiceChannelId}>!"
                    }

                // Are we already playing something in another channel already?
                val voiceConnectionStatus = m.getLorittaVoiceConnectionStateOrNull(guildId) ?: context.fail {
                    // Looks like something went wrong! Took too long to get if I'm in a voice channel or not
                    content = "Deu ruim!"
                }

                val lorittaConnectedVoiceChannelId = voiceConnectionStatus.channelId?.let { Snowflake(it) }
                if (voiceConnectionStatus.playing && voiceConnectionStatus.channelId != null && lorittaConnectedVoiceChannelId != userConnectedVoiceChannelId)
                    context.fail {
                        // We are already playing in another channel!
                        content = "Eu já estou tocando áudio em outro canal! <#${voiceConnectionStatus.channelId}>"
                    }

                // Wait until we have a non-empty model list
                val models = falatronModelsManager.models.filter { it.isNotEmpty() }
                    .first()

                val model = models.firstOrNull { it.name == args[Options.voice] }
                    ?: context.fail {
                        styled(
                            "Voz desconhecida!",
                            Emotes.LoriSob
                        )
                    }

                val uniqueNotificationId2 = UUID.randomUUID().toString()

                val message = context.sendMessage {
                    styled(
                        "Pedindo para o Falatron gerar as vozes...",
                        Emotes.LoriLick
                    )
                }

                m.services.notify(
                    FalatronVoiceRequest(
                        uniqueNotificationId2,
                        context.guildId.toLong(),
                        userConnectedVoiceChannelId.toLong(),
                        args[Options.voice],
                        args[Options.text]
                    )
                )

                val receivedResponse = withTimeoutOrNull(5_000) {
                    m.filterNotificationsByUniqueId(uniqueNotificationId2)
                        .filterIsInstance<FalatronVoiceRequestReceivedResponseX>()
                        .first()
                }

                if (receivedResponse == null) {
                    message.editMessage {
                        styled(
                            "Parece que deu algum problema e eu não recebi o pedido de geração de voz! Bug?",
                            Emotes.LoriSleeping
                        )
                    }
                    return@coroutineScope
                }

                // Timeout job
                launch {
                    delay(3.minutes) // If it takes more than 3 minutes to complete...

                    // Edit the message to indicate that something went wrong...
                    message.editMessage {
                        styled(
                            "Parece que algo deu errado!",
                            Emotes.LoriSleeping
                        )
                    }

                    // And then cancel the scope!
                    this@coroutineScope.cancel()
                }

                m.filterNotificationsByUniqueId(uniqueNotificationId2)
                    .filterIsInstance<FalatronNotification>()
                    .collect {
                        when (it) {
                            is FalatronOfflineErrorResponse -> {
                                message.editMessage {
                                    styled(
                                        "Parece que o Falatron está instável ou offline... Tente novamente mais tarde!",
                                        Emotes.LoriSleeping
                                    )
                                }
                                cancel()
                            }
                            is FailedToConnectToVoiceChannelResponse -> {
                                message.editMessage {
                                    styled(
                                        "Eu não consegui entrar no canal de voz... Se eu estou conectada no canal de voz, tente me desconectar do canal e use o comando novamente!",
                                        Emotes.LoriSob
                                    )
                                }
                                cancel()
                            }
                            is FalatronVoiceResponse -> {
                                message.editMessage {
                                    styled(
                                        "Voz gerada com sucesso! Tocando em <#${userConnectedVoiceChannelId}>",
                                        Emotes.LoriHi
                                    )

                                    embed {
                                        author(model.author)
                                        url = "https://falatron.com/"

                                        title = model.name
                                        description = "*${args[Options.text]}*"

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
                                cancel()
                            }
                            else -> {}
                        }
                    }

                // TODO: Add a timeout somewhere to avoid suspending this forever if something goes extraordinarily wrong
            }
        } catch (e: CancellationException) {
            // Will be thrown when cancelling the flow
        }
    }
}