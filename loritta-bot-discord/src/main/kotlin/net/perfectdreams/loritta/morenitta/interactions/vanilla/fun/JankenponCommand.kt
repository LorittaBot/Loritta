package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference

class JankenponCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Jankenpon
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("pedrapapeltesoura")
            add("jankenpon")
            add("ppt")
        }

        executor = JankenponCommandExecutor()
    }

    inner class JankenponCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val choice = string("choice", I18N_PREFIX.Options.Action) {
                choice(I18N_PREFIX.Rock, "rock")
                choice(I18N_PREFIX.Paper, "paper")
                choice(I18N_PREFIX.Scissors, "scissors")
                choice(I18N_PREFIX.JesusChrist, "jesus")
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val choice = when (val choiceResolvable = args[options.choice]) {
                "jesus" -> "jesus"
                "papel" -> "paper"
                "pedra" -> "rock"
                "tesoura" -> "scissors"
                else -> choiceResolvable
            }
            val janken = Jankenpon.getJanken(choice)

            if (janken != null) {
                val opponent = Jankenpon.entries[context.loritta.random.nextInt(Jankenpon.entries.size)]
                val status = janken.getStatus(opponent)
                val fancy = buildString {
                    when (status) {
                        Jankenpon.JankenponStatus.WIN -> {
                            append("**${context.i18nContext.get(I18N_PREFIX.Win)} ")
                            append(Emotes.LoriWow.asMention + "**")
                        }
                        Jankenpon.JankenponStatus.LOSE -> {
                            append("**${context.i18nContext.get(I18N_PREFIX.Lose)} ")
                            append(Emotes.LoriPat.asMention + "**")
                        }
                        Jankenpon.JankenponStatus.DRAW -> {
                            append("**${context.i18nContext.get(I18N_PREFIX.Draw)} ")
                            append(Emotes.LoriSmile.asMention + "**")
                        }
                    }
                }

                val jankenPrefix = when (status) {
                    Jankenpon.JankenponStatus.WIN -> Emotes.Tada
                    Jankenpon.JankenponStatus.DRAW -> Emotes.WhiteFlag
                    Jankenpon.JankenponStatus.LOSE -> Emotes.BlackFlag
                }

                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Chosen(janken.getEmoji(), opponent.getEmoji())),
                        jankenPrefix
                    )

                    styled(fancy)
                }
            } else {
                if (choice.equals("jesus", ignoreCase = true)) {
                    val jesus = "${Emotes.Jesus} *${context.i18nContext.get(I18N_PREFIX.JesusChrist)}* ${Emotes.Jesus}"

                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Chosen(jesus, jesus)),
                            Emotes.WhiteFlag
                        )

                        styled("**${context.i18nContext.get(I18N_PREFIX.MaybeDraw)} ${Emotes.Thinking} ${Emotes.Shrug}**")
                    }
                } else {
                    val fancy = "**${context.i18nContext.get(I18N_PREFIX.InvalidChoice)} \uD83D\uDE09**"
                    val jesus = "🙇 *${context.i18nContext.get(I18N_PREFIX.JesusChrist)}* 🙇"

                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Chosen("\uD83D\uDCA9", jesus)),
                            Emotes.BlackFlag
                        )

                        styled(fancy)
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val choice = args.getOrNull(0)

            if (choice == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.choice to choice
            )
        }
    }

    enum class Jankenpon(var lang: StringI18nData, var wins: StringI18nData, var loses: StringI18nData) {
        ROCK(I18N_PREFIX.Rock, I18N_PREFIX.Scissors, I18N_PREFIX.Paper),
        PAPER(I18N_PREFIX.Paper, I18N_PREFIX.Rock, I18N_PREFIX.Scissors),
        SCISSORS(I18N_PREFIX.Scissors, I18N_PREFIX.Paper, I18N_PREFIX.Rock);

        fun getStatus(janken: Jankenpon): JankenponStatus {
            if (this.name.equals(janken.loses)) {
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
                    valueOf(str.uppercase())
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    }
}