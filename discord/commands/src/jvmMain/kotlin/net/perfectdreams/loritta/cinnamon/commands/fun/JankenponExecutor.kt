package net.perfectdreams.loritta.cinnamon.commands.`fun`

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emote
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.styled
import kotlin.random.Random

class JankenponExecutor(val random: Random, ): CommandExecutor() {
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
                        append(Emotes.loriWow.asMention + "**")
                    }
                    Jankenpon.JankenponStatus.LOSE -> {
                        append("**${context.i18nContext.get(JankenponCommand.I18N_PREFIX.Lose)} ")
                        append(Emotes.loriPat.asMention + "**")
                    }
                    Jankenpon.JankenponStatus.DRAW -> {
                        append("**${context.i18nContext.get(JankenponCommand.I18N_PREFIX.Draw)} ")
                        append(Emotes.loriSmile.asMention + "**")
                    }
                }
            }

            val jankenPrefix = when (status) {
                Jankenpon.JankenponStatus.WIN -> Emotes.tada
                Jankenpon.JankenponStatus.DRAW -> Emotes.whiteFlag
                Jankenpon.JankenponStatus.LOSE -> Emotes.blackFlag
            }

            context.sendMessage {
                styled(
                    prefix = jankenPrefix,
                    content = context.i18nContext.get(JankenponCommand.I18N_PREFIX.Chosen(janken.getEmoji(), opponent.getEmoji()))
                )

                styled(fancy)
            }
        } else {
            if (argument.equals("jesus", ignoreCase = true)) {
                val jesus = "${Emotes.jesus} *${context.i18nContext.get(JankenponCommand.I18N_PREFIX.JesusChrist)}* ${Emotes.jesus}"

                context.sendMessage {
                    styled(
                        prefix = Emotes.whiteFlag,
                        content = context.i18nContext.get(JankenponCommand.I18N_PREFIX.Chosen(jesus, jesus))
                    )

                    styled("**${context.i18nContext.get(JankenponCommand.I18N_PREFIX.MaybeDraw)} ${Emotes.thinking} ${Emotes.shrug}**")
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

        fun getEmoji(): Emote {
            return when (this) {
                ROCK -> Emotes.rock
                PAPER -> Emotes.newspaper
                SCISSORS -> Emotes.scissors
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

