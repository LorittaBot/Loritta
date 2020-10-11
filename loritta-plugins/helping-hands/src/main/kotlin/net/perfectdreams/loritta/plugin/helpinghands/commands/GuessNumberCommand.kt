package net.perfectdreams.loritta.plugin.helpinghands.commands

import com.mrpowergamerbr.loritta.Loritta
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.GenericReplies
import net.perfectdreams.loritta.utils.PaymentUtils
import net.perfectdreams.loritta.utils.SonhosPaymentReason

class GuessNumberCommand(plugin: HelpingHandsPlugin) : DiscordAbstractCommandBase(
        plugin.loritta,
        listOf("guessnumber", "adivinharnumero", "adivinharnúmero"),
        CommandCategory.ECONOMY
) {
    companion object {
        const val VICTORY_PRIZE = 1_000L
        const val LOSE_PRIZE = 115L
    }

    override fun command() = create {
        localizedDescription("commands.economy.guessnumber.description")

        usage {
            arguments {
                argument(ArgumentType.NUMBER) {}
            }
        }

        examples {
            + "10"
            + "3"
        }

        executesDiscord {
            // Gets the first argument
            // If the argument is null (we just show the command explanation and exit)
            // If it is not null, we convert it to a Integer (if it is a invalid number, it will be null)
            // Then, in the ".also" block, we check if it is null and, if it is, we show that the user provided a invalid number!
            val number = (args.getOrNull(0) ?: explainAndExit()).toIntOrNull()
                    .also {
                        if (it == null)
                            GenericReplies.invalidNumber(this, args[0])
                    }

            if (number !in 1..10)
                fail(locale["commands.economy.guessnumber.numberNotInRange", VICTORY_PRIZE])

            if (number != null) {
                val randomNumber = Loritta.RANDOM.nextInt(1, 11)
                val won = number == randomNumber
                val profile = lorittaUser.profile

                if (won) {
                    loritta.newSuspendedTransaction {
                        profile.addSonhosNested(VICTORY_PRIZE)

                        PaymentUtils.addToTransactionLogNested(
                                VICTORY_PRIZE,
                                SonhosPaymentReason.GUESS_NUMBER,
                                receivedBy = user.idLong
                        )
                    }

                    reply(locale["commands.economy.guessnumber.youWin", VICTORY_PRIZE], Emotes.LORI_RICH)
                } else {
                    loritta.newSuspendedTransaction {
                        profile.takeSonhosNested(LOSE_PRIZE)

                        PaymentUtils.addToTransactionLogNested(
                                LOSE_PRIZE,
                                SonhosPaymentReason.GUESS_NUMBER,
                                givenBy = user.idLong
                        )
                    }

                    reply(locale.getList("commands.economy.guessnumber.youLose", randomNumber, LOSE_PRIZE).random(), Emotes.LORI_CRYING)
                }
            }
        }
    }
}