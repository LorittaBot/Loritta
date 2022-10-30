package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.GACampaigns
import net.perfectdreams.loritta.morenitta.utils.GenericReplies
import net.perfectdreams.loritta.morenitta.utils.NumberUtils
import net.perfectdreams.loritta.morenitta.utils.sendStyledReply

class EmojiFightBetCommand(val m: LorittaBot) : DiscordAbstractCommandBase(
    m,
    listOf("emojifight bet", "rinhadeemoji bet", "emotefight bet"),
    net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY
) {
    override fun command() = create {
        localizedDescription("commands.command.emojifightbet.description")
        localizedExamples("commands.command.emojifightbet.examples")

        usage {
            arguments {
                argument(ArgumentType.NUMBER) {}
                argument(ArgumentType.NUMBER) {
                    optional = true
                }
            }
        }

        this.similarCommands = listOf("EmojiFightCommand")
        this.canUseInPrivateChannel = false

        executesDiscord {
            // Gets the first argument
            // If the argument is null (we just show the command explanation and exit)
            // If it is not null, we convert it to a Long (if it is a invalid number, it will be null)
            // Then, in the ".also" block, we check if it is null and, if it is, we show that the user provided a invalid number!
            val totalEarnings = (args.getOrNull(0) ?: explainAndExit())
                .let { NumberUtils.convertShortenedNumberToLong(it) }
                .let {
                    if (it == null)
                        GenericReplies.invalidNumber(this, args[0])
                    it
                }

            if (0 >= totalEarnings)
                fail(locale["commands.command.flipcoinbet.zeroMoney"], Constants.ERROR)

            val selfUserProfile = lorittaUser.profile

            if (totalEarnings > selfUserProfile.money) {
                sendStyledReply {
                    this.append {
                        message = locale["commands.command.flipcoinbet.notEnoughMoneySelf"]
                        prefix = Constants.ERROR
                    }

                    this.append {
                        message = GACampaigns.sonhosBundlesUpsellDiscordMessage(
                            "https://loritta.website/", // Hardcoded, woo
                            "bet-coinflip-legacy",
                            "bet-not-enough-sonhos"
                        )
                        prefix = Emotes.LORI_RICH.asMention
                        mentionUser = false
                    }
                }
                return@executesDiscord
            }

            // Only allow users to participate in a emoji fight bet if the user got their daily reward today
            AccountUtils.getUserTodayDailyReward(loritta, lorittaUser.profile)
                ?: fail(
                    locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", serverConfig.commandPrefix],
                    Constants.ERROR
                )

            // Self user check
            run {
                val epochMillis = user.timeCreated.toEpochSecond() * 1000

                // Don't allow users to bet if they are recent accounts
                if (epochMillis + (Constants.ONE_WEEK_IN_MILLISECONDS * 2) > System.currentTimeMillis()) // 14 dias
                    fail(
                        LorittaReply(
                            locale["commands.command.pay.selfAccountIsTooNew", 14] + " ${Emotes.LORI_CRYING}",
                            Constants.ERROR
                        )
                    )
            }

            val maxPlayersInEvent = (
                    (this.args.getOrNull(1)?.toIntOrNull() ?: EmojiFight.DEFAULT_MAX_PLAYER_COUNT)
                        .coerceIn(2, EmojiFight.DEFAULT_MAX_PLAYER_COUNT)
                    )

            val emojiFight = EmojiFight(
                this,
                totalEarnings,
                maxPlayersInEvent
            )

            emojiFight.start()
        }
    }
}