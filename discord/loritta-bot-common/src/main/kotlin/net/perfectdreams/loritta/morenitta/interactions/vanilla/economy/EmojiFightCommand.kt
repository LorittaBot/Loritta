package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.economy.EmojiFight
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.*

class EmojiFightCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY) {
        isGuildOnly = true

        executor = BanInfoExecutor()
    }

    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Emojifight
    }

    inner class BanInfoExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val sonhos = optionalString(
                "sonhos",
                I18N_PREFIX.Options.Sonhos.Text
            )

            val maxPlayers = optionalLong(
                "max_players",
                I18N_PREFIX.Options.MaxPlayers.Text,
                requiredRange = 2..EmojiFight.DEFAULT_MAX_PLAYER_COUNT.toLong()
            )
        }

        override val options = Options()

        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            val selfUserProfile = context.lorittaUser.profile

            // Gets the first argument
            // If the argument is null (we just show the command explanation and exit)
            // If it is not null, we convert it to a Long (if it is a invalid number, it will be null)
            // Then, in the ".also" block, we check if it is null and, if it is, we show that the user provided a invalid number!
            val providedStringSonhosInput = args[options.sonhos]

            val totalEarnings = if (providedStringSonhosInput != null) {
                val sonhos = providedStringSonhosInput.let { NumberUtils.convertShortenedNumberToLong(it) }
                    ?: context.fail(
                        true,
                        context.i18nContext.get(
                            I18nKeysData.Commands.InvalidNumber(providedStringSonhosInput)
                        ),
                        Emotes.LORI_CRYING.asMention
                    )

                sonhos
            } else {
                null
            }

            // Sonhos check if the user provided sonhos' amount
            if (totalEarnings != null) {
                if (totalEarnings > selfUserProfile.money) {
                    context.fail(true) {
                        this.styled(
                            context.locale["commands.command.flipcoinbet.notEnoughMoneySelf"],
                            Constants.ERROR
                        )

                        this.styled(
                            context.i18nContext.get(
                                GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                    "https://loritta.website/", // Hardcoded, woo
                                    "bet-coinflip-legacy",
                                    "bet-not-enough-sonhos"
                                )
                            ),
                            Emotes.LORI_RICH.asMention
                        )
                    }
                }

                // Only allow users to participate in a emoji fight bet if the user got their daily reward today
                val todayDailyReward = AccountUtils.getUserTodayDailyReward(loritta, selfUserProfile)
                if (todayDailyReward == null) {
                    context.fail(true) {
                        styled(
                            context.locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", context.config.commandPrefix],
                            Constants.ERROR
                        )
                    }
                }

                // Self user check
                run {
                    val epochMillis = context.user.timeCreated.toEpochSecond() * 1000

                    // Don't allow users to bet if they are recent accounts
                    if (epochMillis + (Constants.ONE_WEEK_IN_MILLISECONDS * 2) > System.currentTimeMillis()) { // 14 dias
                        context.fail(true) {
                            styled(
                                context.locale["commands.command.pay.selfAccountIsTooNew", 14] + " ${Emotes.LORI_CRYING}",
                                Constants.ERROR
                            )
                        }
                    }
                }
            }

            val maxPlayersInEvent = args[options.maxPlayers]?.toInt() ?: EmojiFight.DEFAULT_MAX_PLAYER_COUNT

            val emojiFight = EmojiFight(
                CommandContextCompat.InteractionsCommandContextCompat(context),
                totalEarnings,
                maxPlayersInEvent
            )

            emojiFight.start()
        }
    }
}