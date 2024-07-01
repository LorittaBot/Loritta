package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.EmbedBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.disabledButton
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.selectMenu
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions.transactiontransformers.*
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.serializable.*
import kotlin.math.ceil

class TransactionsExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    companion object {
        private const val TRANSACTIONS_PER_PAGE = 10

        suspend fun createMessage(
            loritta: LorittaBot,
            i18nContext: I18nContext,
            userId: Snowflake,
            viewingTransactionsOfUserId: Snowflake,
            page: Long,
            userFacingTransactionTypeFilter: List<TransactionType>
        ): suspend MessageBuilder.() -> (Unit) = {
            // If the list is empty, we will use *all* transaction types in the filter
            // This makes it easier because you don't need to manually deselect every single filter before you can filter by a specific
            // transaction type.
            val transactionTypeFilter = userFacingTransactionTypeFilter.ifEmpty { TransactionType.values().toList() }

            val transactions = loritta.pudding.sonhos.getUserTransactions(
                UserId(viewingTransactionsOfUserId),
                transactionTypeFilter,
                TRANSACTIONS_PER_PAGE,
                (page * TRANSACTIONS_PER_PAGE)
            )

            val totalTransactions = loritta.pudding.sonhos.getUserTotalTransactions(
                UserId(viewingTransactionsOfUserId),
                transactionTypeFilter
            )

            val totalPages = ceil((totalTransactions / TRANSACTIONS_PER_PAGE.toDouble())).toLong()

            val isSelf = viewingTransactionsOfUserId.value == userId.value

            val cachedUserInfo = loritta.getCachedUserInfo(viewingTransactionsOfUserId) ?: error("Missing cached user info!")

            content = i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.NotAllTransactionsAreHere)

            if (page >= totalPages && totalPages != 0L) {
                // ===[ EASTER EGG: USER INPUT TOO MANY PAGES ]===
                apply(
                    createTooManyPagesMessage(
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
                        // ===[ NORMAL TRANSACTION VIEW ]===
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
                        // ===[ NO MATCHING TRANSACTIONS VIEW ]===
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

                actionRow {
                    if (addLeftButton) {
                        interactiveButton(
                            ButtonStyle.Primary,
                            ChangeTransactionPageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                ChangeTransactionPageData(
                                    userId,
                                    viewingTransactionsOfUserId,
                                    page - 1,
                                    userFacingTransactionTypeFilter
                                )
                            )
                        ) {
                            loriEmoji = Emotes.ChevronLeft
                        }
                    } else {
                        disabledButton(ButtonStyle.Primary) {
                            loriEmoji = Emotes.ChevronLeft
                        }
                    }

                    if (addRightButton) {
                        interactiveButton(
                            ButtonStyle.Primary,
                            ChangeTransactionPageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                ChangeTransactionPageData(
                                    userId,
                                    viewingTransactionsOfUserId,
                                    page + 1,
                                    userFacingTransactionTypeFilter
                                )
                            )
                        ) {
                            loriEmoji = Emotes.ChevronRight
                        }
                    } else {
                        disabledButton(ButtonStyle.Primary) {
                            loriEmoji = Emotes.ChevronRight
                        }
                    }
                }

                actionRow {
                    selectMenu(
                        ChangeTransactionFilterSelectMenuExecutor,
                        ComponentDataUtils.encode(
                            ChangeTransactionFilterData(
                                userId,
                                viewingTransactionsOfUserId,
                                page,
                                userFacingTransactionTypeFilter
                            )
                        )
                    ) {
                        val transactionTypes = TransactionType.values()
                        this.allowedValues = 1..(25.coerceAtMost(transactionTypes.size))

                        for (transactionType in transactionTypes) {
                            option(i18nContext.get(transactionType.title), transactionType.name) {
                                description = i18nContext.get(transactionType.description)
                                loriEmoji = transactionType.emote
                                default = transactionType in userFacingTransactionTypeFilter
                            }
                        }
                    }
                }
            }
        }

        private suspend fun EmbedBuilder.createTransactionViewEmbed(
            loritta: LorittaBot,
            i18nContext: I18nContext,
            isSelf: Boolean,
            cachedUserInfo: CachedUserInfo,
            transactions: List<SonhosTransaction>,
            page: Long,
            totalTransactions: Long
        ) {
            // ===[ NORMAL TRANSACTION VIEW ]===
            val cachedUserInfos = mutableMapOf<UserId, CachedUserInfo?>(
                cachedUserInfo.id to cachedUserInfo
            )

            title = buildString {
                if (isSelf)
                    append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.YourTransactions))
                else append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UserTransactions("${cachedUserInfo.name.stripCodeBackticks()}#${cachedUserInfo?.discriminator}")))

                append(" — ")

                append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(page + 1)))
            }

            color = LorittaColors.LorittaAqua.toKordColor()

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

                        // ===[ POWERSTREAM ]===
                        is PowerStreamClaimedFirstSonhosRewardSonhosTransaction -> PowerStreamClaimedFirstSonhosRewardTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)
                        is PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction -> PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ LORI COOL CARDS ]===
                        is LoriCoolCardsBoughtBoosterPackSonhosTransaction -> LoriCoolCardsBoughtBoosterPackSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)
                        is LoriCoolCardsFinishedAlbumSonhosTransaction -> LoriCoolCardsFinishedAlbumSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)
                        is LoriCoolCardsPaymentSonhosTradeTransaction -> LoriCoolCardsPaymentSonhosTradeTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

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
            cachedUserInfo: CachedUserInfo?,
        ): EmbedBuilder.() -> (Unit) = {
            title = buildString {
                if (isSelf)
                    append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.YourTransactions))
                else append(i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UserTransactions("${cachedUserInfo?.name?.stripCodeBackticks()}#${cachedUserInfo?.discriminator}")))
            }

            color = LorittaColors.LorittaRed.toKordColor()

            description = i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.NoTransactionsFunnyMessages).random()

            image = "https://stuff.loritta.website/emotes/lori-sob.png"
        }

        suspend fun createTooManyPagesMessage(
            i18nContext: I18nContext,
            userId: Snowflake,
            viewingTransactionsOfUserId: Snowflake,
            totalPages: Long,
            transactionTypeFilter: List<TransactionType>
        ): MessageBuilder.() -> (Unit) = {
            embed {
                title = i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UnknownPage.Title)

                description = i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UnknownPage.Description)
                    .joinToString("\n")

                color = LorittaColors.LorittaRed.toKordColor()

                // TODO: Host this somewhere else
                image = "https://cdn.discordapp.com/attachments/513405772911345664/930945637841788958/fon_final_v3_sticker_small.png"
            }

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    ChangeTransactionPageButtonClickExecutor,
                    ComponentDataUtils.encode(
                        ChangeTransactionPageData(
                            userId,
                            viewingTransactionsOfUserId,
                            totalPages - 1,
                            transactionTypeFilter
                        )
                    )
                ) {
                    label = i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UnknownPage.GoToTheLastPage)
                    loriEmoji = Emotes.LoriSob
                }
            }
        }
    }

    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = optionalUser("user", SonhosCommand.TRANSACTIONS_I18N_PREFIX.Options.User.Text)

        val page = optionalInteger("page", SonhosCommand.TRANSACTIONS_I18N_PREFIX.Options.Page.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val userId = args[options.user]?.id ?: context.user.id
        val page = ((args[options.page] ?: 1L) - 1)
            .coerceAtLeast(0)

        val message = createMessage(
            context.loritta,
            context.i18nContext,
            context.user.id,
            userId,
            page,
            listOf() // Empty = All
        )

        context.sendMessage {
            message()
        }
    }
}