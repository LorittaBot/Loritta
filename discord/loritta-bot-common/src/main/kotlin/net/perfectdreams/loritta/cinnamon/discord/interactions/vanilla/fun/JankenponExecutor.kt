package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.morenitta.LorittaBot

class JankenponExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val value = string("value", JankenponCommand.I18N_PREFIX.Options.Action) {
            choice(JankenponCommand.I18N_PREFIX.Rock, "rock")
            choice(JankenponCommand.I18N_PREFIX.Paper, "paper")
            choice(JankenponCommand.I18N_PREFIX.Scissors, "scissors")
            choice(JankenponCommand.I18N_PREFIX.JesusChrist, "jesus")
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val argument = args[options.value]
        val janken = Jankenpon.getJanken(argument)

        if (janken != null) {
            val opponent = Jankenpon.values()[loritta.random.nextInt(Jankenpon.values().size)]

            val status = janken.getStatus(opponent)

            val fancy = buildString {
                when (status) {
                    Jankenpon.JankenponStatus.WIN -> {
                        append("**${context.i18nContext.get(JankenponCommand.I18N_PREFIX.Win)} ")
                        append(Emotes.LoriWow.asMention + "**")
                    }

                    Jankenpon.JankenponStatus.LOSE -> {
                        append("**${context.i18nContext.get(JankenponCommand.I18N_PREFIX.Lose)} ")
                        append(Emotes.LoriPat.asMention + "**")
                    }

                    Jankenpon.JankenponStatus.DRAW -> {
                        append("**${context.i18nContext.get(JankenponCommand.I18N_PREFIX.Draw)} ")
                        append(Emotes.LoriSmile.asMention + "**")
                    }
                }
            }

            val jankenPrefix = when (status) {
                Jankenpon.JankenponStatus.WIN -> Emotes.Tada
                Jankenpon.JankenponStatus.DRAW -> Emotes.WhiteFlag
                Jankenpon.JankenponStatus.LOSE -> Emotes.BlackFlag
            }

            context.sendMessage {
                styled(
                    prefix = jankenPrefix,
                    content = context.i18nContext.get(
                        JankenponCommand.I18N_PREFIX.Chosen(
                            janken.getEmoji(),
                            opponent.getEmoji()
                        )
                    )
                )

                styled(fancy)
            }
        } else {
            if (argument.equals("jesus", ignoreCase = true)) {
                val jesus =
                    "${Emotes.Jesus} *${context.i18nContext.get(JankenponCommand.I18N_PREFIX.JesusChrist)}* ${Emotes.Jesus}"

                context.sendMessage {
                    styled(
                        prefix = Emotes.WhiteFlag,
                        content = context.i18nContext.get(JankenponCommand.I18N_PREFIX.Chosen(jesus, jesus))
                    )

                    styled("**${context.i18nContext.get(JankenponCommand.I18N_PREFIX.MaybeDraw)} ${Emotes.Thinking} ${Emotes.Shrug}**")
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
                ROCK -> Emotes.Rock
                PAPER -> Emotes.Newspaper
                SCISSORS -> Emotes.Scissors
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

