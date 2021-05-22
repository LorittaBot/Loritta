package net.perfectdreams.loritta.plugin.helpinghands.commands

import com.mrpowergamerbr.loritta.Loritta
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.GenericReplies
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
        localizedDescription("commands.command.guessnumber.description")
        localizedExamples("commands.command.guessnumber.examples")

        usage {
            arguments {
                argument(ArgumentType.NUMBER) {}
            }
        }

        executesDiscord {
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

            if (LOSE_PRIZE > profile.money)
                fail(locale["commands.command.guessnumber.notEnoughSonhos"])

            val randomNumber = Loritta.RANDOM.nextInt(1, 11)
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