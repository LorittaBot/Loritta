package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers.*
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.*
import kotlin.math.ceil

class SonhosTransactionsExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        private const val TRANSACTIONS_PER_PAGE = 10

        fun createMessage(
            loritta: LorittaBot,
            context: UnleashedContext,
            i18nContext: I18nContext,
            userId: Long,
            viewingTransactionsOfUserId: Long,
            page: Long,
            userFacingTransactionTypeFilter: List<TransactionType>
        ): suspend InlineMessage<*>.() -> (Unit) = {
            content = ""

            // If the list is empty, we will use *all* transaction types in the filter
            // This makes it easier because you don't need to manually deselect every single filter before you can filter by a specific
            // transaction type.
            val transactionTypeFilter = userFacingTransactionTypeFilter.ifEmpty { TransactionType.entries.toList() }

            val transactions = loritta.pudding.sonhos.getUserTransactions(
                UserId(viewingTransactionsOfUserId),
                transactionTypeFilter,
                TRANSACTIONS_PER_PAGE,
                (page * TRANSACTIONS_PER_PAGE),
                null,
                null
            )

            val totalTransactions = loritta.pudding.sonhos.getUserTotalTransactions(
                UserId(viewingTransactionsOfUserId),
                transactionTypeFilter,
                null,
                null
            )

            val totalPages = ceil((totalTransactions / TRANSACTIONS_PER_PAGE.toDouble())).toLong()
            val isSelf = viewingTransactionsOfUserId == userId
            HarmonyLoggerFactory.logger {}.value.info { "SonhosTransactionsExecutor#retrieveUserInfoById - UserId: $viewingTransactionsOfUserId" }
            val cachedUserInfo = loritta.lorittaShards.retrieveUserInfoById(viewingTransactionsOfUserId) ?: error("Missing cached user info!")

            if (page >= totalPages && totalPages != 0L) {
                apply(
                    createTooManyPagesMessage(
                        loritta,
                        context,
                        i18nContext,
                        userId,
                        viewingTransactionsOfUserId,
                        totalPages,
                        userFacingTransactionTypeFilter
                    )
                )
            } else {
                embed {
                    if (totalPages != 0L) {
                        createTransactionViewEmbed(
                            loritta,
                            i18nContext,
                            isSelf,
                            cachedUserInfo,
                            transactions,
                            page,
                            totalTransactions
                        )
                    } else {
                        apply(
                            createNoMatchingTransactionsEmbed(
                                loritta,
                                i18nContext,
                                isSelf,
                                cachedUserInfo
                            )
                        )
                    }
                }

                val addLeftButton = page != 0L && totalTransactions != 0L
                val addRightButton = totalPages > (page + 1) && totalTransactions != 0L

                actionRow(
                    // This action row is for the pagination buttons.
                    if (addLeftButton) {
                        loritta.interactivityManager.buttonForUser(
                            userId,
                            context.alwaysEphemeral,
                            ButtonStyle.PRIMARY,
                            "",
                            {
                                loriEmoji = Emotes.ChevronLeft
                            }
                        ) {
                            changePage(
                                it,
                                loritta,
                                userId,
                                viewingTransactionsOfUserId,
                                page - 1,
                                userFacingTransactionTypeFilter
                            )
                        }
                    } else {
                        loritta.interactivityManager.disabledButton(
                            context.alwaysEphemeral,
                            ButtonStyle.PRIMARY,
                            ""
                        ) {
                            loriEmoji = Emotes.ChevronLeft
                        }
                    },

                    if (addRightButton) {
                        loritta.interactivityManager.buttonForUser(
                            userId,
                            context.alwaysEphemeral,
                            ButtonStyle.PRIMARY,
                            "",
                            {
                                loriEmoji = Emotes.ChevronRight
                            }
                        ) {
                            changePage(
                                it,
                                loritta,
                                userId,
                                viewingTransactionsOfUserId,
                                page + 1,
                                userFacingTransactionTypeFilter
                            )
                        }
                    } else {
                        loritta.interactivityManager.disabledButton(
                            context.alwaysEphemeral,
                            ButtonStyle.PRIMARY,
                            ""
                        ) {
                            loriEmoji = Emotes.ChevronRight
                        }
                    }
                )

                actionRow(
                    // This action row is for select menu to filter transactions.
                    loritta.interactivityManager.stringSelectMenuForUser(
                        userId,
                        context.alwaysEphemeral,
                        {
                            val transactionTypes = TransactionType.entries
                            maxValues = 25
                            minValues = 0

                            for (transactionType in transactionTypes) {
                                option(
                                    i18nContext.get(
                                        transactionType.title
                                    ),
                                    transactionType.name,
                                    description = i18nContext.get(
                                        transactionType.description
                                    ),
                                    emoji = loritta.emojiManager.get(transactionType.emote).toJDA(),
                                    default = transactionType in userFacingTransactionTypeFilter
                                )
                            }
                        }
                    ) { context, strings ->
                        val hook = context.updateMessageSetLoadingState()

                        val builtMessage = createMessage(
                            loritta,
                            context,
                            context.i18nContext,
                            userId,
                            viewingTransactionsOfUserId,
                            0, // Change the page to zero when changing the current filter
                            strings.map { TransactionType.valueOf(it) }
                        )

                        val asMessageEditData = MessageEdit {
                            builtMessage()
                        }

                        hook.editOriginal(asMessageEditData).queue()
                    }
                )
            }
        }

        private suspend fun InlineEmbed.createTransactionViewEmbed(
            loritta: LorittaBot,
            i18nContext: I18nContext,
            isSelf: Boolean,
            cachedUserInfo: CachedUserInfo,
            transactions: List<SonhosTransaction>,
            page: Long,
            totalTransactions: Long
        ) {
            val cachedUserInfos = mutableMapOf<UserId, net.perfectdreams.loritta.morenitta.utils.CachedUserInfo?>(
                UserId(cachedUserInfo.id) to cachedUserInfo
            )

            title = buildString {
                if (isSelf)
                    append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.YourTransactions))
                else append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UserTransactions(cachedUserInfo.name.stripCodeBackticks())))

                append(" — ")

                append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(page + 1)))
            }

            color = LorittaColors.LorittaAqua.rgb

            description = buildString {
                for (transaction in transactions) {
                    val transformer = when (transaction) {
                        // ===[ PAYMENTS ]===
                        is PaymentSonhosTransaction -> PaymentSonhosTransactionTransformer

                        // ===[ PAYMENTS (THIRD PARTY / DEPRECATED) ]===
                        is APIInitiatedPaymentSonhosTransaction -> APIInitiatedPaymentSonhosTransactionTransformer

                        // ===[ PAYMENTS (THIRD PARTY) ]===
                        is ThirdPartyPaymentSonhosTransaction -> ThirdPartyPaymentSonhosTransactionTransformer

                        // ===[ DAILY REWARD ]===
                        is DailyRewardSonhosTransaction -> DailyRewardSonhosTransactionTransformer

                        // ===[ BROKER ]===
                        is BrokerSonhosTransaction -> BrokerSonhosTransactionTransformer

                        // ===[ COIN FLIP BET ]===
                        is CoinFlipBetSonhosTransaction -> CoinFlipBetSonhosTransactionTransformer

                        // ===[ COIN FLIP BET GLOBAL ]===
                        is CoinFlipBetGlobalSonhosTransaction -> CoinFlipBetGlobalSonhosTransactionTransformer

                        // ===[ EMOJI FIGHT BET ]===
                        is EmojiFightBetSonhosTransaction -> EmojiFightBetSonhosTransactionTransformer

                        // ===[ RAFFLE ]===
                        is RaffleRewardSonhosTransaction -> RaffleRewardSonhosTransactionTransformer
                        is RaffleTicketsSonhosTransaction -> RaffleTicketsSonhosTransactionTransformer

                        // ===[ SPARKLYPOWER LSX ]===
                        is SparklyPowerLSXSonhosTransaction -> SparklyPowerLSXSonhosTransactionTransformer

                        // ===[ SONHOS BUNDLES ]===
                        is SonhosBundlePurchaseSonhosTransaction -> SonhosBundlePurchaseSonhosTransactionTransformer
                        is ChargebackedSonhosBundleTransaction -> ChargebackedSonhosTransactionTransformer

                        // ===[ DAILY TAX ]===
                        is DailyTaxSonhosTransaction -> DailyTaxSonhosTransactionTransformer

                        // ===[ DIVINE INTERVENTION ]===
                        is DivineInterventionSonhosTransaction -> DivineInterventionSonhosTransactionTransformer

                        // ===[ BOT VOTE ]===
                        is BotVoteSonhosTransaction -> BotVoteTransactionTransformer

                        // ===[ SHIP EFFECT ]===
                        is ShipEffectSonhosTransaction -> ShipEffectSonhosTransactionTransformer

                        // ===[ CHRISTMAS 2022 ]===
                        is Christmas2022SonhosTransaction -> Christmas2022SonhosTransactionTransformer

                        // ===[ EASTER 2023 ]===
                        is Easter2023SonhosTransaction -> Easter2023SonhosTransactionTransformer

                        // ===[ REACTION EVENTS ]===
                        is ReactionEventSonhosTransaction -> ReactionEventSonhosTransactionTransformer

                        // ===[ POWERSTREAM ]===
                        is PowerStreamClaimedFirstSonhosRewardSonhosTransaction -> PowerStreamClaimedFirstSonhosRewardTransactionTransformer
                        is PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction -> PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransactionTransformer

                        // ===[ LORI COOL CARDS ]===
                        is LoriCoolCardsBoughtBoosterPackSonhosTransaction -> LoriCoolCardsBoughtBoosterPackSonhosTransactionTransformer
                        is LoriCoolCardsFinishedAlbumSonhosTransaction -> LoriCoolCardsFinishedAlbumSonhosTransactionTransformer
                        is LoriCoolCardsPaymentSonhosTradeTransaction -> LoriCoolCardsPaymentSonhosTradeTransactionTransformer

                        // ===[ LORITTA ITEM SHOP ]===
                        is LorittaItemShopBoughtBackgroundTransaction -> LorittaItemShopBoughtBackgroundSonhosTransactionTransformer
                        is LorittaItemShopBoughtProfileDesignTransaction -> LorittaItemShopBoughtProfileDesignSonhosTransactionTransformer
                        is LorittaItemShopComissionBackgroundTransaction -> LorittaItemShopComissionBackgroundSonhosTransactionTransformer
                        is LorittaItemShopComissionProfileDesignTransaction -> LorittaItemShopComissionProfileDesignSonhosTransactionTransformer

                        // ===[ BOM DIA & CIA ]===
                        is BomDiaECiaCallCalledTransaction -> BomDiaECiaCallCalledSonhosTransactionTransformer
                        is BomDiaECiaCallWonTransaction -> BomDiaECiaCallWonSonhosTransactionTransformer

                        // ===[ GARTICBOT ]===
                        is GarticosTransferTransaction -> GarticosTransferSonhosTransactionTransformer

                        // ===[ MARRIAGE ]===
                        is MarriageMarryTransaction -> MarriageMarrySonhosTransactionTransformer
                        is MarriageRestoreTransaction -> MarriageRestoreSonhosTransactionTransformer
                        is MarriageRestoreAutomaticTransaction -> MarriageRestoreAutomaticSonhosTransactionTransformer
                        is MarriageLoveLetterTransaction -> MarriageLoveLetterTransactionTransformer

                        is VacationModeLeaveTransaction -> VacationModeLeaveSonhosTransactionTransformer

                        // ===[ REPUTATION DELETE ]===
                        is ReputationDeletedTransaction -> ReputationDeleteTransactionTransformer

                        // ===[ BLACKJACK ]===
                        is BlackjackJoinedTransaction -> SimpleSonhosTransactionTransformers.BlackjackJoinedTransactionTransformer
                        is BlackjackSplitTransaction -> SimpleSonhosTransactionTransformers.BlackjackSplitTransactionTransformer
                        is BlackjackPayoutTransaction -> SimpleSonhosTransactionTransformers.BlackjackPayoutTransactionTransformer
                        is BlackjackTiedTransaction -> SimpleSonhosTransactionTransformers.BlackjackTiedTransactionTransformer
                        is BlackjackInsuranceTransaction -> SimpleSonhosTransactionTransformers.BlackjackInsuranceTransactionTransformer
                        is BlackjackInsurancePayoutTransaction -> SimpleSonhosTransactionTransformers.BlackjackInsurancePayoutTransactionTransformer
                        is BlackjackDoubleDownTransaction -> SimpleSonhosTransactionTransformers.BlackjackDoubleDownTransactionTransformer
                        is BlackjackRefundTransaction -> SimpleSonhosTransactionTransformers.BlackjackRefundTransactionTransformer

                        // This should never happen because we do a left join with a "isNotNull" check
                        is UnknownSonhosTransaction -> UnknownSonhosTransactionTransformer
                    }

                    append("[<t:${transaction.timestamp.epochSeconds}:d> <t:${transaction.timestamp.epochSeconds}:t> | <t:${transaction.timestamp.epochSeconds}:R>]")
                    append(" ")
                    transformer.transformGeneric(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction).invoke(this)
                    append("\n")
                }
            }

            footer(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.TransactionsQuantity(totalTransactions)))
        }

        private fun createNoMatchingTransactionsEmbed(
            loritta: LorittaBot,
            i18nContext: I18nContext,
            isSelf: Boolean,
            cachedUserInfo: CachedUserInfo?
        ): InlineEmbed.() -> (Unit) = {
            title = buildString {
                if (isSelf)
                    append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.YourTransactions))
                else append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UserTransactions("${cachedUserInfo?.name?.stripCodeBackticks()}#${cachedUserInfo?.discriminator}")))
            }

            color = LorittaColors.LorittaRed.rgb

            description = i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.NoTransactionsFunnyMessages).random()

            image = "https://stuff.loritta.website/emotes/lori-sob.png"
        }

        suspend fun createTooManyPagesMessage(
            loritta: LorittaBot,
            context: UnleashedContext,
            i18nContext: I18nContext,
            userId: Long,
            viewingTransactionsOfUserId: Long,
            totalPages: Long,
            transactionTypeFilter: List<TransactionType>
        ): InlineMessage<*>.() -> (Unit) = {
            embed {
                title = i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UnknownPage.Title)

                description = i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UnknownPage.Description)
                    .joinToString("\n")

                color = LorittaColors.LorittaRed.rgb

                image = "https://stuff.loritta.website/lori-fon-bunda.png"
            }

            actionRow(
                loritta.interactivityManager.buttonForUser(
                    userId,
                    context.alwaysEphemeral,
                    ButtonStyle.PRIMARY,
                    i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UnknownPage.GoToTheLastPage),
                    {
                        loriEmoji = Emotes.LoriSob
                    }
                ) {
                    changePage(
                        it,
                        loritta,
                        userId,
                        viewingTransactionsOfUserId,
                        totalPages - 1,
                        transactionTypeFilter
                    )
                }
            )
        }

        suspend fun changePage(
            context: ComponentContext,
            loritta: LorittaBot,
            userId: Long,
            viewingTransactionsOfUserId: Long,
            page: Long,
            transactionTypeFilter: List<TransactionType>
        ) {
            val hook = context.updateMessageSetLoadingState()

            val builtMessage = createMessage(
                loritta,
                context,
                context.i18nContext,
                userId,
                viewingTransactionsOfUserId,
                page,
                transactionTypeFilter
            )

            val asMessageEditData = MessageEdit {
                builtMessage()
            }

            hook.editOriginal(asMessageEditData).await()
        }
    }

    inner class Options : ApplicationCommandOptions() {
        val user = optionalUser("user", SonhosCommand.TRANSACTIONS_I18N_PREFIX.Options.User.Text)
        val page = optionalLong("page", SonhosCommand.TRANSACTIONS_I18N_PREFIX.Options.Page.Text)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        val userId = args[options.user]?.user?.idLong ?: context.user.idLong
        val page = ((args[options.page] ?: 1L) - 1)
            .coerceAtLeast(0)

        val message = createMessage(
            loritta,
            context,
            context.i18nContext,
            context.user.idLong,
            userId,
            page,
            emptyList()
        )

        context.reply(false) {
            message()
        }
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?> {
        val user = context.getUser(0) ?: context.user
        val page = args.getOrNull(0)?.toLongOrNull() ?: args.getOrNull(1)?.toLongOrNull() ?: 1

        return mapOf(
            options.user to UserAndMember(
                user,
                null
            ),
            options.page to page
        )
    }
}
