package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import java.util.*

class GuessNumberCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Guessnumber
        const val VICTORY_PRIZE = 1_000L
        const val LOSE_PRIZE = 115L
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("d6e4c8f5-9b7a-4c3d-8e2f-1a5b6c7d8e9f")) {
        enableLegacyMessageSupport = true

        alternativeLegacyLabels.apply {
            add("adivinharnumero")
            add("adivinharnúmero")
        }

        examples = I18N_PREFIX.Examples

        executor = GuessNumberExecutor()
    }

    inner class GuessNumberExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val number = long("number", I18N_PREFIX.Options.Number.Text, 1L..10L)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (SonhosUtils.checkIfEconomyIsDisabled(context))
                return

            val number = args[options.number]

            // While Discord will validate the argument, we still need this here for the legacy command!
            if (number !in 1..10) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.NumberNotInRange),
                        Emotes.LoriHm
                    )
                }
            }

            val profile = context.lorittaUser.profile

            if (LOSE_PRIZE > profile.money) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.NotEnoughSonhos),
                        Constants.ERROR
                    )

                    styled(
                        context.i18nContext.get(
                            GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                loritta.config.loritta.dashboard.url,
                                "guess-number",
                                "bet-not-enough-sonhos"
                            )
                        ),
                        Emotes.LoriRich
                    )
                }
                return
            }

            val randomNumber = LorittaBot.RANDOM.nextInt(1, 11)
            val won = number.toInt() == randomNumber

            if (won) {
                loritta.newSuspendedTransaction {
                    profile.addSonhosAndAddToTransactionLogNested(
                        VICTORY_PRIZE,
                        SonhosPaymentReason.GUESS_NUMBER,
                    )
                }

                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.YouWin(VICTORY_PRIZE)),
                        Emotes.LoriRich
                    )
                }
            } else {
                loritta.newSuspendedTransaction {
                    profile.takeSonhosAndAddToTransactionLogNested(
                        LOSE_PRIZE,
                        SonhosPaymentReason.GUESS_NUMBER,
                    )
                }

                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.YouLose(randomNumber, LOSE_PRIZE)).random(),
                        Emotes.LoriSob
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val numberAsString = args.getOrNull(0)
            if (numberAsString == null) {
                context.explain()
                return null
            }

            val number = numberAsString.toLongOrNull()
            if (number == null) {
                context.fail(true) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.InvalidNumber(numberAsString)),
                        Emotes.LoriHm
                    )
                }
            }

            return mapOf(
                options.number to number
            )
        }
    }
}
