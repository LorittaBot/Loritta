package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberChannels
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentVibes
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import org.jetbrains.exposed.sql.selectAll
import kotlin.math.absoluteValue

class CreateVideoVibesScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val channelId: Long,
    private val contentCategory: LoriTuberVideoContentCategory,
    private val contentLength: LoriTuberContentLength,
    private val contentVibes: Map<LoriTuberVideoContentVibes, Int>,
    private val editingVibe: LoriTuberVideoContentVibes,
) : LoriTuberScreen(command, user, hook) {
    companion object {
        private val MAX_POINTS_ALLOCATION = 20

        val VIBES_WRAPPER = mutableMapOf(
            LoriTuberVideoContentVibes.VIBE1 to VibeWrapper(
                "Sério",
                "Engraçado"
            ),
            LoriTuberVideoContentVibes.VIBE2 to VibeWrapper(
                "Didático",
                "Divertido"
            ),
            LoriTuberVideoContentVibes.VIBE3 to VibeWrapper(
                "Realista",
                "Fofo"
            ),
            LoriTuberVideoContentVibes.VIBE4 to VibeWrapper(
                "Familiar",
                "Excêntrico"
            ),
            LoriTuberVideoContentVibes.VIBE5 to VibeWrapper(
                "Tranquilo",
                "Agitado"
            ),
            LoriTuberVideoContentVibes.VIBE6 to VibeWrapper(
                "Seguro",
                "Polêmico"
            ),
            LoriTuberVideoContentVibes.VIBE7 to VibeWrapper(
                "Amador",
                "Profissional"
            )
        )
    }

    override suspend fun render() {
        val result = loritta.transaction {
            val channel = LoriTuberChannels.selectAll()
                .where {
                    LoriTuberChannels.id eq channelId
                }
                .firstOrNull()

            if (channel == null)
                return@transaction CreateVideoVibesResult.UnknownChannel

            return@transaction CreateVideoVibesResult.Channel(
                LoriTuberChannel(
                    channel[LoriTuberChannels.id].value,
                    channel[LoriTuberChannels.name]
                )
            )
        }

        when (result) {
            CreateVideoVibesResult.UnknownChannel -> {
                // Channel does not exist! Maybe it was deleted?
                command.switchScreen(
                    CreateChannelScreen(
                        command,
                        user,
                        hook,
                        character
                    )
                )
            }
            is CreateVideoVibesResult.Channel -> {
                val allocatedPoints = contentVibes.values.sumOf { it.absoluteValue }

                val continueButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Continuar",
                    {
                        disabled = contentCategory == null || contentLength == null
                    }
                ) {
                    command.switchScreen(
                        CreateVideoEndScreen(
                            command,
                            user,
                            it.deferEdit(),
                            character,
                            channelId,
                            // Shouldn't be null here
                            contentCategory,
                            contentLength,
                            contentVibes
                        )
                    )
                }

                val viewChannelButton = loritta.interactivityManager.buttonForUser(
                    user,
                    ButtonStyle.PRIMARY,
                    "Voltar",
                    {
                        emoji = Emoji.fromUnicode("\uD83C\uDF9E️")
                    }
                ) {
                    command.switchScreen(
                        CreateVideoVibesScreen(
                            command,
                            user,
                            it.deferEdit(),
                            character,
                            result.channel.id,
                            contentCategory,
                            contentLength,
                            contentVibes,
                            editingVibe,
                        )
                    )
                }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Criação de Vídeos [3/4]"

                            description = buildString {
                                appendLine("Escolha a vibe do vídeo")
                                appendLine()
                                appendLine("```")
                                var idx = 0
                                val longestToneLeft = VIBES_WRAPPER.map { it.value.toneLeft }.maxOf { it.length }
                                val longestToneRight = VIBES_WRAPPER.map { it.value.toneRight }.maxOf { it.length }
                                for (vibe in VIBES_WRAPPER) {
                                    append("${idx + 1}. ")
                                    val vibeValue = contentVibes[vibe.key] ?: -1
                                    if (vibeValue == -1)
                                        append("✓")
                                    else
                                        append(" ")
                                    append(" ")
                                    append("${vibe.value.toneLeft.padEnd(longestToneLeft, ' ')} ${vibe.value.toneRight.padStart(longestToneRight, ' ')} ")
                                    if (vibeValue == 1)
                                        append("✓")
                                    else
                                        append(" ")
                                    appendLine()
                                    idx++
                                }
                                appendLine("```")
                            }
                        }

                        actionRow(
                            loritta.interactivityManager.stringSelectMenu({
                                for (type in LoriTuberVideoContentVibes.entries) {
                                    addOption(type.name, type.name)
                                }

                                setDefaultValues(editingVibe.name)
                            }) { it, values ->
                                command.switchScreen(
                                    CreateVideoVibesScreen(
                                        command,
                                        user,
                                        it.deferEdit(),
                                        character,
                                        result.channel.id,
                                        contentCategory,
                                        contentLength,
                                        contentVibes,
                                        LoriTuberVideoContentVibes.valueOf(values.first()),
                                    )
                                )
                            }
                        )

                        actionRow(
                            loritta.interactivityManager.buttonForUser(
                                user,
                                ButtonStyle.SUCCESS,
                                VIBES_WRAPPER[editingVibe]!!.toneLeft,
                                {}
                            ) { context ->
                                val newVibes = contentVibes.toMutableMap()
                                newVibes[editingVibe] = -1

                                command.switchScreen(
                                    CreateVideoVibesScreen(
                                        command,
                                        user,
                                        context.deferEdit(),
                                        character,
                                        result.channel.id,
                                        contentCategory,
                                        contentLength,
                                        newVibes,
                                        editingVibe,
                                    )
                                )
                            },
                            loritta.interactivityManager.buttonForUser(
                                user,
                                ButtonStyle.SUCCESS,
                                VIBES_WRAPPER[editingVibe]!!.toneRight,
                                {}
                            ) { context ->
                                val newVibes = contentVibes.toMutableMap()
                                newVibes[editingVibe] = 1

                                command.switchScreen(
                                    CreateVideoVibesScreen(
                                        command,
                                        user,
                                        context.deferEdit(),
                                        character,
                                        result.channel.id,
                                        contentCategory,
                                        contentLength,
                                        newVibes,
                                        editingVibe,
                                    )
                                )
                            }
                        )

                        actionRow(continueButton)
                        actionRow(viewChannelButton)
                    }
                ).setReplace(true).await()
            }
        }
    }

    private fun createVibeBar(amount: Int): String {
        val negativeValue = if (0 > amount)
            amount
        else
            0

        val positiveValue = if (amount > 0)
            amount
        else
            0

        return buildString {
            append("[")
            append(createAbsoluteVibeBarHalf(negativeValue).reversed())
            append(" | ")
            append(createAbsoluteVibeBarHalf(positiveValue))
            append("]")
        }
    }

    private fun createAbsoluteVibeBarHalf(amount: Int): String {
        val abs = amount.absoluteValue
        return buildString {
            repeat(5) {
                if (abs - 1 >= it)
                    append("█")
                else
                    append("░")
            }
        }
    }

    data class VibeWrapper(
        val toneLeft: String,
        val toneRight: String
    )

    sealed class CreateVideoVibesResult {
        data object UnknownChannel : CreateVideoVibesResult()
        data class Channel(val channel: LoriTuberChannel) : CreateVideoVibesResult()
    }
}