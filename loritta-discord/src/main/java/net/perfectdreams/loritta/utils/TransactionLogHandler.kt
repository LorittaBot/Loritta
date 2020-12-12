package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.perfectdreams.loritta.tables.SonhosTransaction
import org.jetbrains.exposed.sql.ResultRow

/**
 * Class used to make transaction logs more
 * flexible and affordable, designed to support as much as possible transaction types.
 */
object TransactionLogHandler {

    /**
     * Primary method of the class, here we'll retrieve a log for the respective
     * provided data.
     *
     * @param [sender] The user that's requesting the data
     * @param [locale] The locale that will be used to retrieve the sentences
     * @param [transaction] The transaction result row used to retrieve its data
     */
    suspend fun getLogByTransactionData(sender: Long, locale: BaseLocale, transaction: ResultRow): String {
        val isReceiver = transaction[SonhosTransaction.receivedBy] == sender

        return if (transaction.getTransactionReason().multipleUsersRequired)
            handleMultipleUserTransaction(locale, transaction, isReceiver)
        else
            handleGenericTransactionReason(locale, transaction, isReceiver)
    }

    /**
     * This handles all transactions with the reason [SonhosPaymentReason.PAYMENT]
     * using the provided data.
     *
     * @param [locale] The locale that will be used to retrieve the sentences
     * @param [transaction] The transaction result row used to retrieve its data
     * @param [isReceiver] True if the user that's requesting the data is the transaction sender
     */
    private suspend fun handleMultipleUserTransaction(locale: BaseLocale, transaction: ResultRow, isReceiver: Boolean): String {
        val receivedByUserId = if (isReceiver) {
            transaction[SonhosTransaction.givenBy]
        } else {
            transaction[SonhosTransaction.receivedBy]
        }

        val receivedByUser = lorittaShards.retrieveUserInfoById(receivedByUserId)

        val name = ("${receivedByUser?.name}#${receivedByUser?.discriminator} ($receivedByUserId)")
        val type = transaction.getTransactionReason()

        return if (isReceiver) {
            locale["commands.economy.transactions.${getLocaleApplicableName(type.name)}.received", transaction[SonhosTransaction.quantity], "`$name`"]
        } else {
            locale["commands.economy.transactions.${getLocaleApplicableName(type.name)}.sent", transaction[SonhosTransaction.quantity], "`$name`"]
        }
    }

    /**
     * Method used to parse all the usual (not custom) logs
     *
     * @param [locale] The locale that will be used to retrieve the sentences
     * @param [transaction] The transaction result row used to retrieve its data
     * @param [isReceiver] True if the user that's requesting the data is the transaction sender
     */
    private fun handleGenericTransactionReason(locale: BaseLocale, transaction: ResultRow, isReceiver: Boolean): String {
        val type = transaction.getTransactionReason()
        val genericTypeName = locale["commands.economy.transactions.types.${getLocaleApplicableName(type.name)}"]

        return when {
            type.overrideLocaleName != null -> locale[type.overrideLocaleName, transaction[SonhosTransaction.quantity], genericTypeName]
            isReceiver -> locale["commands.economy.transactions.genericReceived", transaction[SonhosTransaction.quantity], genericTypeName]
            else -> locale["commands.economy.transactions.genericSent", transaction[SonhosTransaction.quantity], genericTypeName]
        }
    }

    /**
     * This will enhance the provided [origin] string
     * to make locale parses possible
     *
     * @param origin The original string
     */
    private fun getLocaleApplicableName(origin: String) = origin
            .toLowerCase()
            .replace("_", " ")
            .split(" ")
            .joinToString("") {
                it.capitalize()
            }
            .toCharArray().apply {
                this[0] = this[0].toLowerCase()
            }
            .joinToString("")

    /**
     * Just a simple extension to remove ""boilerplate""
     * from code that will only work on this class
     */
    private fun ResultRow.getTransactionReason(): SonhosPaymentReason =
            this[SonhosTransaction.reason]

}