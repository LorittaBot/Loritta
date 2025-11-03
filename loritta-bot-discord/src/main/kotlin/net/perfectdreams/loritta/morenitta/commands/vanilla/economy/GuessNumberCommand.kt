package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.morenitta.utils.GenericReplies
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.morenitta.utils.sendStyledReply

class GuessNumberCommand(plugin: LorittaBot) : DiscordAbstractCommandBase(
    plugin,
    listOf("guessnumber", "adivinharnumero", "adivinharnúmero"),
    net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY
) {
    companion object {
        const val VICTORY_PRIZE = 1_000L
        const val LOSE_PRIZE = 115L
    }

    override fun command() = create {
        localizedDescription("commands.command.guessnumber.description")
        localizedExamples("commands.command.guessnumber.examples")

        usage {
            arguments {
                argument(ArgumentType.NUMBER) {}
            }
        }

        executesDiscord {
            if (SonhosUtils.checkIfEconomyIsDisabled(this))
                return@executesDiscord

            // Gets the first argument
            // If the argument is null (we just show the command explanation and exit)
            // If it is not null, we convert it to a Integer (if it is a invalid number, it will be null)
            // Then, in the ".let" block, we check if it is null and, if it is, we show that the user provided a invalid number!
            val number = (args.getOrNull(0) ?: explainAndExit()).toIntOrNull()
                .let {
                    if (it == null)
                        GenericReplies.invalidNumber(this, args[0])
                    it
                }

            if (number !in 1..10)
                fail(locale["commands.command.guessnumber.numberNotInRange", VICTORY_PRIZE])

            val profile = lorittaUser.profile

            if (LOSE_PRIZE > profile.money) {
                sendStyledReply {
                    this.append {
                        message = locale["commands.command.guessnumber.notEnoughSonhos"]
                        prefix = Constants.ERROR
                    }

                    this.append {
                        message = i18nContext.get(
                            GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                loritta.config.loritta.dashboard.url,
                                "guess-number-legacy",
                                "bet-not-enough-sonhos"
                            )
                        )
                        prefix = Emotes.LORI_RICH.asMention
                        mentionUser = false
                    }
                }
                return@executesDiscord
            }

            val randomNumber = LorittaBot.RANDOM.nextInt(1, 11)
            val won = number == randomNumber

            if (won) {
                loritta.newSuspendedTransaction {
                    profile.addSonhosAndAddToTransactionLogNested(
                        VICTORY_PRIZE,
                        SonhosPaymentReason.GUESS_NUMBER,
                    )
                }

                reply(locale["commands.command.guessnumber.youWin", VICTORY_PRIZE], Emotes.LORI_RICH)
            } else {
                loritta.newSuspendedTransaction {
                    profile.takeSonhosAndAddToTransactionLogNested(
                        LOSE_PRIZE,
                        SonhosPaymentReason.GUESS_NUMBER,
                    )
                }

                reply(locale.getList("commands.command.guessnumber.youLose", randomNumber, LOSE_PRIZE).random(), Emotes.LORI_CRYING)
            }
        }
    }
}