package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.transactiontransformers

import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.serializable.BlackjackDoubleDownTransaction
import net.perfectdreams.loritta.serializable.BlackjackInsurancePayoutTransaction
import net.perfectdreams.loritta.serializable.BlackjackInsuranceTransaction
import net.perfectdreams.loritta.serializable.BlackjackJoinedTransaction
import net.perfectdreams.loritta.serializable.BlackjackPayoutTransaction
import net.perfectdreams.loritta.serializable.BlackjackRefundTransaction
import net.perfectdreams.loritta.serializable.BlackjackSplitTransaction
import net.perfectdreams.loritta.serializable.BlackjackTiedTransaction

object SimpleSonhosTransactionTransformers {
    val BlackjackDoubleDownTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackDoubleDownTransaction>(false) { _, i18nContext, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.DoubleDown(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackRefundTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackRefundTransaction>(true) { _, i18nContext, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Refunded(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackInsuranceTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackInsuranceTransaction>(false) { _, i18nContext, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Insurance(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackInsurancePayoutTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackInsurancePayoutTransaction>(true) { _, i18nContext, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.InsurancePayout(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackTiedTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackTiedTransaction>(true) { _, i18nContext, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Tied(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackJoinedTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackJoinedTransaction>(false) { _, i18nContext, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Joined(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackSplitTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackSplitTransaction>(false) { _, i18nContext, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Split(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }

    val BlackjackPayoutTransactionTransformer = SimpleSonhosTransactionTransformer<BlackjackPayoutTransaction>(true) { _, i18nContext, transaction ->
        append(
            i18nContext.get(
                SonhosCommand.TRANSACTIONS_I18N_PREFIX.Types.Blackjack.Payout(quantity = transaction.sonhos, matchId = transaction.matchId)
            )
        )
    }
}