package net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions

import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.message.EmbedBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.common.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.TransactionsCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers.BrokerSonhosTransactionTransformer
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers.CoinFlipBetGlobalSonhosTransactionTransformer
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers.CoinFlipBetSonhosTransactionTransformer
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers.DailyTaxSonhosTransactionTransformer
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers.DivineInterventionSonhosTransactionTransformer
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers.EmojiFightBetSonhosTransactionTransformer
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers.PaymentSonhosTransactionTransformer
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers.ShipEffectSonhosTransactionTransformer
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers.SonhosBundlePurchaseSonhosTransactionTransformer
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers.SparklyPowerLSXSonhosTransactionTransformer
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.transactiontransformers.UnknownSonhosTransactionTransformer
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.components.selectMenu
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.CoinFlipBetGlobalSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.CoinFlipBetSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.DailyTaxSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.DivineInterventionSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.EmojiFightBetSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.PaymentSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.ShipEffectSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.SonhosBundlePurchaseSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.SonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.SparklyPowerLSXSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UnknownSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import kotlin.math.ceil

class TransactionsExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val user = optionalUser("user", TransactionsCommand.I18N_PREFIX.Options.User.Text)
                .register()

            val page = optionalInteger("page", TransactionsCommand.I18N_PREFIX.Options.Page.Text)
                .register()
        }

        override val options = Options

        private const val TRANSACTIONS_PER_PAGE = 10

        suspend fun createMessage(
            loritta: LorittaCinnamon,
            i18nContext: I18nContext,
            data: TransactionListData
        ): suspend MessageBuilder.() -> (Unit) = {
            val transactions = loritta.services.sonhos.getUserTransactions(
                data.viewingTransactionsOfUserId,
                data.transactionTypeFilter,
                TRANSACTIONS_PER_PAGE,
                (data.page * TRANSACTIONS_PER_PAGE)
            )

            val totalTransactions = loritta.services.sonhos.getUserTotalTransactions(
                data.viewingTransactionsOfUserId,
                data.transactionTypeFilter
            )

            val totalPages = ceil((totalTransactions / TRANSACTIONS_PER_PAGE.toDouble())).toLong()

            val isSelf = data.viewingTransactionsOfUserId.value == data.userId.value

            val cachedUserInfo = loritta.getCachedUserInfo(data.viewingTransactionsOfUserId) ?: error("Missing cached user info!")

            content = i18nContext.get(TransactionsCommand.I18N_PREFIX.NotAllTransactionsAreHere)

            if (data.page >= totalPages && totalPages != 0L) {
                // ===[ EASTER EGG: USER INPUT TOO MANY PAGES ]===
                apply(
                    createTooManyPagesMessage(
                        i18nContext,
                        data,
                        totalPages
                    )
                )
            } else {
                embed {
                    if (totalPages != 0L) {
                        // ===[ NORMAL TRANSACTION VIEW ]===
                        createTransactionViewEmbed(
                            loritta,
                            i18nContext,
                            data,
                            isSelf,
                            cachedUserInfo,
                            transactions,
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

                val addLeftButton = data.page != 0L && totalTransactions != 0L
                val addRightButton = totalPages > (data.page + 1) && totalTransactions != 0L

                actionRow {
                    if (addLeftButton) {
                        interactiveButton(
                            ButtonStyle.Primary,
                            ChangeTransactionPageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                data.copy(
                                    page = data.page - 1
                                )
                            )
                        ) {
                            loriEmoji = Emotes.ChevronLeft
                        }
                    } else {
                        interactiveButton(
                            ButtonStyle.Primary,
                            ChangeTransactionPageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                data.copy(
                                    page = data.page - 1
                                )
                            )
                        ) {
                            loriEmoji = Emotes.ChevronLeft
                            disabled = true
                        }
                    }

                    if (addRightButton) {
                        interactiveButton(
                            ButtonStyle.Primary,
                            ChangeTransactionPageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                data.copy(
                                    page = data.page + 1
                                )
                            )
                        ) {
                            loriEmoji = Emotes.ChevronRight
                        }
                    } else {
                        interactiveButton(
                            ButtonStyle.Primary,
                            ChangeTransactionPageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                data.copy(
                                    page = data.page + 1
                                )
                            )
                        ) {
                            loriEmoji = Emotes.ChevronRight
                            disabled = true
                        }
                    }
                }

                actionRow {
                    selectMenu(
                        ChangeTransactionFilterSelectMenuExecutor,
                        ComponentDataUtils.encode(data)
                    ) {
                        val transactionTypes = TransactionType.values()
                        this.allowedValues = 1..(25.coerceAtMost(transactionTypes.size))

                        for (transactionType in transactionTypes) {
                            option(i18nContext.get(transactionType.title), transactionType.name) {
                                description = i18nContext.get(transactionType.description)
                                loriEmoji = transactionType.emote
                                default = transactionType in data.transactionTypeFilter
                            }
                        }
                    }
                }
            }
        }

        private suspend fun EmbedBuilder.createTransactionViewEmbed(
            loritta: LorittaCinnamon,
            i18nContext: I18nContext,
            data: TransactionListData,
            isSelf: Boolean,
            cachedUserInfo: CachedUserInfo,
            transactions: List<SonhosTransaction>,
            totalTransactions: Long
        ) {
            // ===[ NORMAL TRANSACTION VIEW ]===
            val cachedUserInfos = mutableMapOf<UserId, CachedUserInfo?>(
                cachedUserInfo.id to cachedUserInfo
            )

            title = buildString {
                if (isSelf)
                    append(i18nContext.get(TransactionsCommand.I18N_PREFIX.YourTransactions))
                else append(i18nContext.get(TransactionsCommand.I18N_PREFIX.UserTransactions("${cachedUserInfo.name.stripCodeBackticks()}#${cachedUserInfo?.discriminator}")))

                append(" — ")

                append(i18nContext.get(TransactionsCommand.I18N_PREFIX.Page(data.page + 1)))
            }

            color = LorittaColors.LorittaAqua.toKordColor()

            description = buildString {
                for (transaction in transactions) {
                    val stringBuilderBlock = when (transaction) {
                        // ===[ PAYMENTS ]===
                        is PaymentSonhosTransaction -> PaymentSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ BROKER ]===
                        is BrokerSonhosTransaction -> BrokerSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ COIN FLIP BET ]===
                        is CoinFlipBetSonhosTransaction -> CoinFlipBetSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ COIN FLIP BET GLOBAL ]===
                        is CoinFlipBetGlobalSonhosTransaction -> CoinFlipBetGlobalSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ EMOJI FIGHT BET ]===
                        is EmojiFightBetSonhosTransaction -> EmojiFightBetSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ SPARKLYPOWER LSX ]===
                        is SparklyPowerLSXSonhosTransaction -> SparklyPowerLSXSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ SONHOS BUNDLES ]===
                        is SonhosBundlePurchaseSonhosTransaction -> SonhosBundlePurchaseSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ DAILY TAX ]===
                        is DailyTaxSonhosTransaction -> DailyTaxSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ DIVINE INTERVENTION ]===
                        is DivineInterventionSonhosTransaction -> DivineInterventionSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // ===[ SHIP EFFECT ]===
                        is ShipEffectSonhosTransaction -> ShipEffectSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)

                        // This should never happen because we do a left join with a "isNotNull" check
                        is UnknownSonhosTransaction -> UnknownSonhosTransactionTransformer.transform(loritta, i18nContext, cachedUserInfo, cachedUserInfos, transaction)
                    }

                    append("[<t:${transaction.timestamp.epochSeconds}:d> <t:${transaction.timestamp.epochSeconds}:t> | <t:${transaction.timestamp.epochSeconds}:R>]")
                    append(" ")
                    stringBuilderBlock()
                    append("\n")
                }
            }

            footer(i18nContext.get(TransactionsCommand.I18N_PREFIX.TransactionsQuantity(totalTransactions)))
        }

        private fun createNoMatchingTransactionsEmbed(
            loritta: LorittaCinnamon,
            i18nContext: I18nContext,
            isSelf: Boolean,
            cachedUserInfo: CachedUserInfo?,
        ): EmbedBuilder.() -> (Unit) = {
            title = buildString {
                if (isSelf)
                    append(i18nContext.get(TransactionsCommand.I18N_PREFIX.YourTransactions))
                else append(i18nContext.get(TransactionsCommand.I18N_PREFIX.UserTransactions("${cachedUserInfo?.name?.stripCodeBackticks()}#${cachedUserInfo?.discriminator}")))
            }

            color = LorittaColors.LorittaRed.toKordColor()

            description = i18nContext.get(TransactionsCommand.I18N_PREFIX.NoTransactionsFunnyMessages).random()

            image = "https://assets.perfectdreams.media/loritta/emotes/lori-sob.png"
        }

        suspend fun createTooManyPagesMessage(
            i18nContext: I18nContext,
            data: TransactionListData,
            totalPages: Long
        ): MessageBuilder.() -> (Unit) = {
            embed {
                title = i18nContext.get(TransactionsCommand.I18N_PREFIX.UnknownPage.Title)

                description = i18nContext.get(TransactionsCommand.I18N_PREFIX.UnknownPage.Description)
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
                        data.copy(
                            page = totalPages - 1
                        )
                    )
                ) {
                    label = i18nContext.get(TransactionsCommand.I18N_PREFIX.UnknownPage.GoToTheLastPage)
                    loriEmoji = Emotes.LoriSob
                }
            }
        }
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val userId = UserId(args[Options.user]?.id?.value ?: context.user.id.value)
        val page = ((args[Options.page] ?: 1L) - 1)
            .coerceAtLeast(0)

        val message = createMessage(
            context.loritta,
            context.i18nContext,
            TransactionListData(
                context.user.id,
                userId,
                page,
                TransactionType.values().toList()
            )
        )

        context.sendMessage {
            message()
        }
    }
}