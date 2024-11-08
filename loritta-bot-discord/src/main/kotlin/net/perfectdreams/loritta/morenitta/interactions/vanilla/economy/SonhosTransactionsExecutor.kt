package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
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
            i18nContext: I18nContext,
            userId: Long,
            viewingTransactionsOfUserId: Long,
            page: Long,
            userFacingTransactionTypeFilter: List<TransactionType>
        ): suspend InlineMessage<*>.() -> (Unit) = {
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
            val cachedUserInfo = loritta.lorittaShards.retrieveUserInfoById(viewingTransactionsOfUserId) ?: error("Missing cached user info!")

            content = i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.NotAllTransactionsAreHere
            )

            if (page >= totalPages && totalPages != 0L) {
                apply(
                    createTooManyPagesMessage(
                        loritta,
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
                            ButtonStyle.PRIMARY,
                            ""
                        ) {
                            loriEmoji = Emotes.ChevronLeft
                        }
                    },

                    if (addRightButton) {
                        loritta.interactivityManager.buttonForUser(
                            userId,
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
                else append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UserTransactions("${cachedUserInfo.name.stripCodeBackticks()}#${cachedUserInfo.discriminator}")))

                append(" — ")

                append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(page + 1)))
            }

            color = LorittaColors.LorittaAqua.rgb

            description = buildString {
                for (transaction in transactions) {
                    val stringBuilderBlock = when (transaction) {
                        // ===[ PAYMENTS ]===
                        is PaymentSonhosTransaction -> PaymentSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ PAYMENTS ]===
                        is DailyRewardSonhosTransaction -> DailyRewardSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ BROKER ]===
                        is BrokerSonhosTransaction -> BrokerSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ COIN FLIP BET ]===
                        is CoinFlipBetSonhosTransaction -> CoinFlipBetSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ COIN FLIP BET GLOBAL ]===
                        is CoinFlipBetGlobalSonhosTransaction -> CoinFlipBetGlobalSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ EMOJI FIGHT BET ]===
                        is EmojiFightBetSonhosTransaction -> EmojiFightBetSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ RAFFLE ]===
                        is RaffleRewardSonhosTransaction -> RaffleRewardSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)
                        is RaffleTicketsSonhosTransaction -> RaffleTicketsSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ SPARKLYPOWER LSX ]===
                        is SparklyPowerLSXSonhosTransaction -> SparklyPowerLSXSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ SONHOS BUNDLES ]===
                        is SonhosBundlePurchaseSonhosTransaction -> SonhosBundlePurchaseSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ DAILY TAX ]===
                        is DailyTaxSonhosTransaction -> DailyTaxSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ DIVINE INTERVENTION ]===
                        is DivineInterventionSonhosTransaction -> DivineInterventionSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ BOT VOTE ]===
                        is BotVoteSonhosTransaction -> BotVoteTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ SHIP EFFECT ]===
                        is ShipEffectSonhosTransaction -> ShipEffectSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ CHRISTMAS 2022 ]===
                        is Christmas2022SonhosTransaction -> Christmas2022SonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ EASTER 2023 ]===
                        is Easter2023SonhosTransaction -> Easter2023SonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ REACTION EVENTS ]===
                        is ReactionEventSonhosTransaction -> ReactionEventSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ POWERSTREAM ]===
                        is PowerStreamClaimedFirstSonhosRewardSonhosTransaction -> PowerStreamClaimedFirstSonhosRewardTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)
                        is PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction -> PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ LORI COOL CARDS ]===
                        is LoriCoolCardsBoughtBoosterPackSonhosTransaction -> LoriCoolCardsBoughtBoosterPackSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)
                        is LoriCoolCardsFinishedAlbumSonhosTransaction -> LoriCoolCardsFinishedAlbumSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)
                        is LoriCoolCardsPaymentSonhosTradeTransaction -> LoriCoolCardsPaymentSonhosTradeTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ LORITTA ITEM SHOP ]===
                        is LorittaItemShopBoughtBackgroundTransaction -> LorittaItemShopBoughtBackgroundSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)
                        is LorittaItemShopBoughtProfileDesignTransaction -> LorittaItemShopBoughtProfileDesignSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ BOM DIA & CIA ]===
                        is BomDiaECiaCallCalledTransaction -> BomDiaECiaCallCalledSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)
                        is BomDiaECiaCallWonTransaction -> BomDiaECiaCallWonSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ GARTICBOT ]===
                        is GarticosTransferTransaction -> GarticosTransferSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ MARRIAGE ]===
                        is MarriageMarryTransaction -> MarriageMarrySonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // This should never happen because we do a left join with a "isNotNull" check
                        is UnknownSonhosTransaction -> UnknownSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)
                    }

                    append("[<t:${transaction.timestamp.epochSeconds}:d> <t:${transaction.timestamp.epochSeconds}:t> | <t:${transaction.timestamp.epochSeconds}:R>]")
                    append(" ")
                    stringBuilderBlock()
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
    ): Map<OptionReference<*>, Any?>? {
        return null
    }

    /* TODO: Implement this when the legacy command is removed

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
     */
}
