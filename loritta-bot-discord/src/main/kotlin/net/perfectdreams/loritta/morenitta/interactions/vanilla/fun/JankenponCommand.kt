package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class JankenponCommand(private val loritta: LorittaBot) : SlashCommandDeclarationWrapper  {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Jankenpon
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN, UUID.fromString("dc3627d3-4151-415d-97d1-8fd0ead91265")) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        this.alternativeLegacyLabels.apply {
            add("pedrapapeltesoura")
            add("ppt")
        }

        executor = JankenponExecutor(loritta)
    }

    class JankenponExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val value = string("value", I18N_PREFIX.Options.Action) {
                choice(I18N_PREFIX.Rock, "rock")
                choice(I18N_PREFIX.Paper, "paper")
                choice(I18N_PREFIX.Scissors, "scissors")
                choice(I18N_PREFIX.JesusChrist, "jesus")
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val argument = args[options.value]
            val janken = Jankenpon.getJanken(argument)

            if (janken != null) {
                val opponent = Jankenpon.entries[loritta.random.nextInt(Jankenpon.entries.size)]

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
                        prefix = jankenPrefix,
                        content = context.i18nContext.get(I18N_PREFIX.Chosen(janken.getEmoji(), opponent.getEmoji()))
                    )

                    styled(fancy)
                }
            } else {
                if (argument.equals("jesus", ignoreCase = true)) {
                    val jesus = "${Emotes.Jesus} *${context.i18nContext.get(I18N_PREFIX.JesusChrist)}* ${Emotes.Jesus}"

                    context.reply(false) {
                        styled(
                            prefix = Emotes.WhiteFlag,
                            content = context.i18nContext.get(I18N_PREFIX.Chosen(jesus, jesus))
                        )

                        styled("**${context.i18nContext.get(I18N_PREFIX.MaybeDraw)} ${Emotes.Thinking} ${Emotes.Shrug}**")
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val arg0 = context.args.getOrNull(0)
            if (arg0 == null) {
                context.explain()
                return null
            }

            val playerValue = context.args[0]

            val janken = Jankenpon.getFromLangString(playerValue.lowercase(), context.i18nContext)
            if (janken == null) {
                context.explain()
                return null
            }

            return mapOf(options.value to janken.name)
        }

        enum class Jankenpon(var lang: StringI18nData, var wins: String, var loses: String) {
            // Os wins e os loses precisam ser uma string já que os enums ainda não foram inicializados
            ROCK(I18N_PREFIX.Rock, "SCISSORS", "PAPER"),
            PAPER(I18N_PREFIX.Paper, "ROCK", "SCISSORS"),
            SCISSORS(I18N_PREFIX.Scissors, "PAPER", "ROCK");

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
                        valueOf(str.uppercase())
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }

                fun getFromLangString(str: String, i18nContext: I18nContext): Jankenpon? {
                    for (janken in Jankenpon.entries) {
                        if (i18nContext.get(janken.lang).equals(str, true)) {
                            return janken
                        }
                    }
                    return null
                }
            }
        }
    }
}