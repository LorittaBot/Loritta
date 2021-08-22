package net.perfectdreams.loritta.commands.`fun`

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.Emotes
import kotlin.random.Random

class JankenponExecutor(val random: Random, val emotes: Emotes): CommandExecutor() {
    companion object: CommandExecutorDeclaration(JankenponExecutor::class) {
        object Options: CommandOptions() {
            val value = string("value", JankenponCommand.I18N_PREFIX.Options.Action)
                .choice("rock", JankenponCommand.I18N_PREFIX.Rock)
                .choice("paper", JankenponCommand.I18N_PREFIX.Paper)
                .choice("scissors", JankenponCommand.I18N_PREFIX.Scissors)
                .choice("jesus", JankenponCommand.I18N_PREFIX.JesusChrist)
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
                        append("**${context.i18nContext.get(JankenponCommand.I18N_PREFIX.Win)} ")
                        append(emotes.loriWow.asMention + "**")
                    }
                    Jankenpon.JankenponStatus.LOSE -> {
                        append("**${context.i18nContext.get(JankenponCommand.I18N_PREFIX.Lose)} ")
                        append(emotes.loriPat.asMention + "**")
                    }
                    Jankenpon.JankenponStatus.DRAW -> {
                        append("**${context.i18nContext.get(JankenponCommand.I18N_PREFIX.Draw)} ")
                        append(emotes.loriSmile.asMention + "**")
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
                    content = context.i18nContext.get(JankenponCommand.I18N_PREFIX.Chosen(janken.getEmoji(emotes), opponent.getEmoji(emotes)))
                )

                styled(fancy)
            }
        } else {
            if (argument.equals("jesus", ignoreCase = true)) {
                val jesus = "${emotes.jesus} *${context.i18nContext.get(JankenponCommand.I18N_PREFIX.JesusChrist)}* ${emotes.jesus}"

                context.sendMessage {
                    styled(
                        prefix = emotes.whiteFlag,
                        content = context.i18nContext.get(JankenponCommand.I18N_PREFIX.Chosen(jesus, jesus))
                    )

                    styled("**${context.i18nContext.get(JankenponCommand.I18N_PREFIX.MaybeDraw)} ${emotes.thinking} ${emotes.shrug}**")
                }
            }
        }
    }

    enum class Jankenpon(var lang: StringI18nData, var wins: String, var loses: String) {
        // Os wins e os loses precisam ser uma string já que os enums ainda não foram inicializados
        ROCK(JankenponCommand.I18N_PREFIX.Rock, "SCISSORS", "PAPER"),
        PAPER(JankenponCommand.I18N_PREFIX.Paper, "ROCK", "SCISSORS"),
        SCISSORS(JankenponCommand.I18N_PREFIX.Scissors, "PAPER", "ROCK");

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

