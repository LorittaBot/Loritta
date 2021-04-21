package net.perfectdreams.loritta.commands.`fun`

import net.perfectdreams.loritta.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import kotlin.random.Random

class JankenponExecutor(val random: Random, val emotes: Emotes): CommandExecutor() {
    companion object: CommandExecutorDeclaration(JankenponExecutor::class) {
        object Options: CommandOptions() {
            val value = string("value", LocaleKeyData("${JankenponCommand.LOCALE_PREFIX}.selectType"))
                .choice("rock", LocaleKeyData(Jankenpon.ROCK.lang))
                .choice("paper", LocaleKeyData(Jankenpon.PAPER.lang))
                .choice("scissors", LocaleKeyData(Jankenpon.SCISSORS.lang))
                .choice("jesus", LocaleKeyData("${JankenponCommand.LOCALE_PREFIX}.jesusChrist"))
                .register()
        }
        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val argument = args[options.value]
        val janken = Jankenpon.getJanken(argument)

        if (janken != null) {
            val opponent = Jankenpon.values()[random.nextInt(Jankenpon.values().size)]

            val status = janken.getStatus(opponent)

            val fancy = buildString {
                when (status) {
                    Jankenpon.JankenponStatus.WIN -> {
                        append("**${context.locale["${JankenponCommand.LOCALE_PREFIX}.win"]} ")
                        append(emotes.blush.asMention + "**")
                    }
                    Jankenpon.JankenponStatus.LOSE -> {
                        append("**${context.locale["${JankenponCommand.LOCALE_PREFIX}.lose"]} ")
                        append(emotes.slightSmile.asMention + "**")
                    }
                    Jankenpon.JankenponStatus.DRAW -> {
                        append("**${context.locale["${JankenponCommand.LOCALE_PREFIX}.draw"]} ")
                        append(emotes.blush.asMention + "**")
                    }
                }
            }

            val jankenPrefix = when (status) {
                Jankenpon.JankenponStatus.WIN -> emotes.tada
                Jankenpon.JankenponStatus.DRAW -> emotes.whiteFlag
                Jankenpon.JankenponStatus.LOSE -> emotes.blackFlag
            }

            context.sendMessage {
                styled(
                    prefix = jankenPrefix,
                    content = context.locale["${JankenponCommand.LOCALE_PREFIX}.chosen", janken.getEmoji(emotes), opponent.getEmoji(emotes)]
                )

                styled(fancy)
            }
        } else {
            if (argument.equals("jesus", ignoreCase = true)) {
                val jesus = "${emotes.jesus} *${context.locale["${JankenponCommand.LOCALE_PREFIX}.jesusChrist"]}* ${emotes.jesus}"

                context.sendMessage {
                    styled(
                        prefix = emotes.whiteFlag,
                        content = context.locale["${JankenponCommand.LOCALE_PREFIX}.chosen", jesus, jesus]
                    )

                    styled("**${context.locale["${JankenponCommand.LOCALE_PREFIX}.maybeDraw"]} ${emotes.thinking} ${emotes.shrug}**")
                }
            }
        }
    }
    enum class Jankenpon(var lang: String, var wins: String, var loses: String) {
        // Os wins e os loses precisam ser uma string já que os enums ainda não foram inicializados
        ROCK("${JankenponCommand.LOCALE_PREFIX}.rock", "SCISSORS", "PAPER"),
        PAPER("${JankenponCommand.LOCALE_PREFIX}.paper", "ROCK", "SCISSORS"),
        SCISSORS("${JankenponCommand.LOCALE_PREFIX}.scissors", "PAPER", "ROCK");

        fun getStatus(janken: Jankenpon): JankenponStatus {
            if (this.name.equals(janken.loses, ignoreCase = true)) {
                return JankenponStatus.WIN
            }
            if (this == janken) {
                return JankenponStatus.DRAW
            }
            return JankenponStatus.LOSE
        }

        fun getEmoji(emotes: Emotes): Emote {
            return when (this) {
                ROCK -> emotes.rock
                PAPER -> emotes.newspaper
                SCISSORS -> emotes.scissors
            }
        }

        enum class JankenponStatus {
            WIN,
            LOSE,
            DRAW
        }

        companion object {
            fun getJanken(str: String): Jankenpon? {
                return try {
                    valueOf(str.toUpperCase())
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    }
}

