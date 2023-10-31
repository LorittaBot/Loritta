package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightParticipants
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.economy.EmojiFight
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.NumberUtils
import org.jetbrains.exposed.sql.*

class EmojiFightCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Emojifight
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY) {
        enableLegacyMessageSupport = true
        isGuildOnly = true

        alternativeLegacyLabels.apply {
            add("emotefight")
        }

        executor = EmojiFightForFunStartExecutor()

        subcommand(I18N_PREFIX.Start.Label, I18N_PREFIX.Start.Description) {
            alternativeLegacyLabels.apply {
                add("bet")
            }

            executor = EmojiFightBetStartExecutor()
        }

        subcommand(I18N_PREFIX.Emoji.Label, I18N_PREFIX.Emoji.Description) {
            executor = EmojiFightChangeEmojiExecutor()
        }

        subcommand(I18N_PREFIX.Stats.Label, I18N_PREFIX.Stats.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("emojifight bet stats")
                add("emotefight bet stats")
            }

            executor = EmojiFightBetStatsExecutor()
        }
    }

    // Only used for message commands
    inner class EmojiFightForFunStartExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val maxPlayers = optionalLong(
                "max_players",
                I18N_PREFIX.Start.Options.MaxPlayers.Text,
                requiredRange = 2..EmojiFight.DEFAULT_MAX_PLAYER_COUNT.toLong()
            )
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val maxPlayersInEvent = args[options.maxPlayers]?.toInt()?.coerceIn(2..EmojiFight.DEFAULT_MAX_PLAYER_COUNT) ?: EmojiFight.DEFAULT_MAX_PLAYER_COUNT

            val emojiFight = EmojiFight(
                context,
                null,
                maxPlayersInEvent
            )

            emojiFight.start()
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val participants = args.getOrNull(0)?.toLongOrNull()

            return mapOf(options.maxPlayers to participants)
        }
    }

    inner class EmojiFightBetStartExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
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

            val maxPlayersInEvent = args[options.maxPlayers]?.toInt()?.coerceIn(2..EmojiFight.DEFAULT_MAX_PLAYER_COUNT) ?: EmojiFight.DEFAULT_MAX_PLAYER_COUNT

            val emojiFight = EmojiFight(
                context,
                totalEarnings,
                maxPlayersInEvent
            )

            emojiFight.start()
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val sonhosQuantity = args.getOrNull(0)
            val participants = args.getOrNull(1)?.toLongOrNull()

            return mapOf(
                options.sonhos to sonhosQuantity,
                options.maxPlayers to participants
            )
        }
    }

    inner class EmojiFightChangeEmojiExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val emoji = optionalString("emoji", I18N_PREFIX.Emoji.Options.Emoji.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
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

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            if (args.isEmpty())
                return mapOf()

            return mapOf(
                options.emoji to args.joinToString(" ")
            )
        }
    }

    inner class EmojiFightBetStatsExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = optionalUser(
                "user",
                I18N_PREFIX.Stats.Options.User.Text
            )
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val user = args[options.user]?.user ?: context.user

            val result = loritta.transaction {
                // This super globby mess is here because Exposed can't behave and select the correct columns
                val innerJoin = EmojiFightParticipants.innerJoin(EmojiFightMatches.innerJoin(EmojiFightMatchmakingResults, { EmojiFightMatches.id }, { EmojiFightMatchmakingResults.match }), { EmojiFightParticipants.match }, { EmojiFightMatches.id })

                // This is a bit harder than coinflip bet
                // The EmojiFightParticipants only includes matches that DID end up happening, matches that failed to be executed (like one player matches) are not included
                // We use the innerJoin variable BECAUSE we want to get ONLY matches that had sonhos involved
                // 0 entry price = just for fun, and we don't want to include these!
                val matchesPlayed = innerJoin.select { EmojiFightParticipants.user eq user.idLong and (EmojiFightMatchmakingResults.entryPrice neq 0) }.count()
                if (matchesPlayed == 0L)
                    return@transaction QueryResult.NotFound

                val matchesWon = innerJoin.select {
                    // Yes, it looks wonky, but it is correct
                    EmojiFightParticipants.user eq user.idLong and (EmojiFightMatchmakingResults.winner eq EmojiFightParticipants.id) and (EmojiFightMatchmakingResults.entryPrice neq 0)
                }.count()
                // Kinda obvious tbh
                val matchesLost = matchesPlayed - matchesWon

                // This is a biiiit harder to figure out due to the lack of proper information on the tables
                var sonhosEarned = 0L
                var sonhosLost = 0L
                var sonhosLostToTaxes = 0L

                val matchesThatWeParticipated = innerJoin.select { EmojiFightParticipants.user eq user.idLong and (EmojiFightMatchmakingResults.entryPrice neq 0) }.toList()
                val matchCountColumn = EmojiFightParticipants.match.count()
                // We need to do this like this to :sparkles: optimize the query :sparkles:, if not then it is TOO SLOW
                val allParticipantsOfTheMatchesThatWeParticipated = EmojiFightParticipants
                    .slice(EmojiFightParticipants.match, matchCountColumn)
                    .select { EmojiFightParticipants.match inList matchesThatWeParticipated.map { it[EmojiFightMatches.id] } }
                    .groupBy(EmojiFightParticipants.match)
                    .toList()

                for (row in matchesThatWeParticipated) {
                    val didWeWinThisMatch = row[EmojiFightParticipants.user].value == user.idLong && row[EmojiFightParticipants.id] == row[EmojiFightMatchmakingResults.winner]
                    if (didWeWinThisMatch) {
                        // We need to multiply by the amount of (players - 1) that participated in the match!
                        val participantCount = allParticipantsOfTheMatchesThatWeParticipated.firstOrNull { it[EmojiFightParticipants.match] == row[EmojiFightMatches.id] }
                            ?.get(matchCountColumn)

                        // Should NEVER be null, but...
                        if (participantCount != null) {
                            sonhosEarned += (row[EmojiFightMatchmakingResults.entryPriceAfterTax] * (participantCount - 1))
                            val tax = row[EmojiFightMatchmakingResults.tax]
                            if (tax != null)
                                sonhosLostToTaxes += tax
                        }
                    } else {
                        // We always pay the full value if the lost
                        sonhosLost += row[EmojiFightMatchmakingResults.entryPrice]
                    }
                }
                val totalSonhos = sonhosEarned - sonhosLost
                val emojiCount = EmojiFightParticipants.emoji.count()
                val bestBichano = innerJoin.slice(EmojiFightParticipants.emoji, emojiCount)
                    .select { EmojiFightParticipants.user eq user.idLong and (EmojiFightMatchmakingResults.winner eq EmojiFightParticipants.id) and (EmojiFightMatchmakingResults.entryPrice neq 0) }
                    .groupBy(EmojiFightParticipants.emoji)
                    .orderBy(emojiCount, SortOrder.DESC)
                    .limit(1)
                    .firstOrNull()?.get(EmojiFightParticipants.emoji)

                QueryResult.Success(
                    matchesPlayed,
                    matchesWon,
                    matchesLost,
                    sonhosEarned,
                    sonhosLost,
                    sonhosLostToTaxes,
                    totalSonhos,
                    bestBichano
                )
            }

            when (result) {
                QueryResult.NotFound -> {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Stats.PlayerHasNeverPlayed),
                            Emotes.LORI_CRYING
                        )
                    }
                }
                is QueryResult.Success -> {
                    context.reply(false) {
                        if (context.user == user) {
                            styled(context.i18nContext.get(I18N_PREFIX.Stats.YourStats), Emotes.LORI_RICH)
                        } else {
                            styled(context.i18nContext.get(I18N_PREFIX.Stats.StatsOfUser(user.asMention)), Emotes.LORI_RICH)
                        }
                        styled(context.i18nContext.get(I18N_PREFIX.Stats.PlayedMatches(result.matchesPlayed)))
                        styled(context.i18nContext.get(I18N_PREFIX.Stats.WonMatches((result.matchesWon / result.matchesPlayed.toDouble()), result.matchesWon)))
                        styled(context.i18nContext.get(I18N_PREFIX.Stats.LostMatches((result.matchesLost / result.matchesPlayed.toDouble()), result.matchesLost)))
                        styled(context.i18nContext.get(I18N_PREFIX.Stats.WonSonhos(result.sonhosEarned)))
                        styled(context.i18nContext.get(I18N_PREFIX.Stats.LostSonhos(result.sonhosLost)))
                        styled(context.i18nContext.get(I18N_PREFIX.Stats.LostSonhosToTaxes(result.sonhosLostToTaxes)))
                        styled(context.i18nContext.get(I18N_PREFIX.Stats.TotalSonhos(result.totalSonhos)))
                        if (result.bestBichano != null) {
                            styled(context.i18nContext.get(I18N_PREFIX.Stats.BestEmoji(result.bestBichano)))
                        }
                        styled(context.i18nContext.get(I18N_PREFIX.Stats.ProbabilityExplanation), Emotes.LORI_COFFEE)
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val userAndMember = context.getUserAndMember(0)

            return mapOf(options.user to userAndMember)
        }
    }

    sealed class QueryResult {
        class Success(
            val matchesPlayed: Long,
            val matchesWon: Long,
            val matchesLost: Long,
            val sonhosEarned: Long,
            val sonhosLost: Long,
            val sonhosLostToTaxes: Long,
            val totalSonhos: Long,
            val bestBichano: String?
        ) : QueryResult()

        object NotFound : QueryResult()
    }
}