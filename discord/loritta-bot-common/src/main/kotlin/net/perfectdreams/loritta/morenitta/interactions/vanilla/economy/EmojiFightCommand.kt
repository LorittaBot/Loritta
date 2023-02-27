package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.economy.EmojiFight
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.NumberUtils

class EmojiFightCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Emojifight
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY) {
        isGuildOnly = true

        subcommand(I18N_PREFIX.Start.Label, I18N_PREFIX.Start.Description) {
            executor = EmojiFightStartExecutor()
        }

        subcommand(I18N_PREFIX.Emoji.Label, I18N_PREFIX.Emoji.Description) {
            executor = EmojiFightChangeEmojiExecutor()
        }
    }

    inner class EmojiFightStartExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val sonhos = optionalString(
                "sonhos",
                I18N_PREFIX.Start.Options.Sonhos.Text
            )

            val maxPlayers = optionalLong(
                "max_players",
                I18N_PREFIX.Start.Options.MaxPlayers.Text,
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
                if (0 >= totalEarnings)
                    context.fail(true) {
                        styled(
                            context.locale["commands.command.flipcoinbet.zeroMoney"],
                            Constants.ERROR
                        )
                    }

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
                AccountUtils.getUserTodayDailyReward(loritta, selfUserProfile)
                    ?: context.fail(true) {
                        styled(
                            context.locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", context.config.commandPrefix],
                            Constants.ERROR
                        )
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

    inner class EmojiFightChangeEmojiExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val emoji = optionalString("emoji", I18N_PREFIX.Emoji.Options.Emoji.Text)
        }

        override val options = Options()

        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            val canUseCustomEmojis = loritta.newSuspendedTransaction {
                UserPremiumPlans.getPlanFromValue(loritta._getActiveMoneyFromDonations(context.user.idLong)).customEmojisInEmojiFight
            }

            if (!canUseCustomEmojis) {
                context.fail(true) {
                    styled(
                        "Apenas usuários com plano premium \"Recomendado\" ou superior podem colocar emojis personalizados no emoji fight!",
                    )
                }
            }

            val newEmojiAsString = args[options.emoji]

            if (newEmojiAsString == null) {
                loritta.newSuspendedTransaction {
                    loritta.getOrCreateLorittaProfile(context.user.idLong)
                        .settings
                        .emojiFightEmoji = null
                }

                context.reply(true) {
                    styled(
                        "Emoji personalizado removido!"
                    )
                }
                return
            }

            val discordEmoji = Emoji.fromFormatted(newEmojiAsString) as? CustomEmoji

            val newEmoji = if (discordEmoji != null) {
                discordEmoji.asMention
            } else {
                val match = loritta.unicodeEmojiManager.regex.find(newEmojiAsString)
                    ?: context.fail(true) {
                        styled(
                            "Não encontrei nenhum emoji na sua mensagem..."
                        )
                    }

                match.value
            }

            loritta.newSuspendedTransaction {
                loritta.getOrCreateLorittaProfile(context.user.idLong)
                    .settings
                    .emojiFightEmoji = newEmojiAsString
            }

            if (discordEmoji == null)
                context.reply(true) {
                    styled(
                        "Emoji alterado! Nas próximas rinhas de emoji, o $newEmoji irá te acompanhar nas suas incríveis batalhas cativantes."
                    )
                }
            else
                context.reply(true) {
                    styled("Emoji alterado! Nas próximas rinhas de emoji, o $newEmoji irá te acompanhar nas suas incríveis batalhas cativantes.")
                    styled("Lembre-se que eu preciso estar no servidor onde o emoji está para eu conseguir usar o emoji!")
                    styled("Observação: Você será banido de usar a Loritta caso você coloque emojis sugestivos ou NSFW. Tenha bom senso e não atrapalhe os servidores dos outros com bobagens!")
                }
        }
    }
}