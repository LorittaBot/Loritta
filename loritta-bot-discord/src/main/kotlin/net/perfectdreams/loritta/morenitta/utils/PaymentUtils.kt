package net.perfectdreams.loritta.morenitta.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredChargebackedSonhosBundleTransaction
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

object PaymentUtils {
    private val logger by HarmonyLoggerFactory.logger {}
    var economyEnabled = true

    fun addToTransactionLogNested(
        quantity: Long,
        reason: SonhosPaymentReason,
        receivedBy: Long? = null,
        givenBy: Long? = null,
        givenAtMillis: Long = System.currentTimeMillis()
    ) {
        if (receivedBy == null && givenBy == null)
            throw IllegalArgumentException("receivedBy and givenBy is null! One of them must NOT be null!")

        SonhosTransaction.insert {
            it[SonhosTransaction.givenBy] = givenBy
            it[SonhosTransaction.receivedBy] = receivedBy
            it[givenAt] = givenAtMillis
            it[SonhosTransaction.quantity] = quantity.toBigDecimal()
            it[SonhosTransaction.reason] = reason
        }

        logger.info { "Added transaction $reason with $quantity that was given by $givenBy and received by $receivedBy at $givenAtMillis with reason $reason" }
    }

    val mutex = Mutex()

    private val List<SonhosRemovalData>?.totalQuantity
        get() = this?.sumOf { it.quantity } ?: 0L

    /**
     * Removes sonhos from [userId] due to a chargeback of sonhos value [quantity]
     *
     * @param loritta              the loritta instance
     * @param userId               the user ID that should be checked
     * @param quantity             the quantity of the chargeback
     * @param removeSonhos         if sonhos should be removed in the check
     * @param notifyChargebackUser if enabled, the user that triggered the chargeback will receive a DM
     * @param notifyUsers          if enabled, users that had their sonhos removed will be notified
     * @return a map containing all the users and sonhos removals that were done
     */
    suspend fun removeSonhosDueToChargeback(
            loritta: LorittaBot,
            userId: Long,
            quantity: Long,
            removeSonhos: Boolean,
            notifyChargebackUser: Boolean,
            notifyUsers: Boolean
    ): Map<Long, MutableList<SonhosRemovalData>> {
        val triggeredSonhos = mutableMapOf<Long, MutableList<SonhosRemovalData>>()

        // We lock in a mutex because a lot of times users spam chargebacks, which could cause issues when taking out money
        mutex.withLock {
            retrieveSonhosRemovalDueToChargeback(
                    loritta,
                    userId,
                    quantity,
                    triggeredSonhos,
                    listOf(),
                    "self"
            )

            if (removeSonhos) {
                for (entry in triggeredSonhos) {
                    // Time to remove all the sonhos
                    val totalQuantity = entry.value.totalQuantity

                    // Yes, this can happen! We should figure out why this happens, but it is not a huge issue so, whatever...
                    if (0L >= totalQuantity)
                        continue

                    val profile = loritta.getOrCreateLorittaProfile(entry.key)
                    loritta.newSuspendedTransaction {
                        logger.info { "Taking $totalQuantity sonhos from ${entry.key} due to chargeback" }

                        // Take the sonhos
                        profile.takeSonhosAndAddToTransactionLogNested(
                            totalQuantity,
                            SonhosPaymentReason.CHARGEBACK,
                            // Maybe the user was spending stuff while we were checking for their sonhos, so let's ignore negative money issues
                            failIfQuantityIsSmallerThanWhatUserHas = false
                        )

                        // Cinnamon transactions log
                        SimpleSonhosTransactionsLogUtils.insert(
                            entry.key,
                            Instant.now(),
                            TransactionType.SONHOS_BUNDLE_PURCHASE,
                            totalQuantity,
                            StoredChargebackedSonhosBundleTransaction(userId)
                        )
                    }
                }
            }

            // Now we need to send a DM to the affected users
            for (entry in triggeredSonhos) {
                // Not you, you are banned from Loritta so you shouldn't even *care* about this.
                if (!notifyUsers || (!notifyChargebackUser && entry.key == userId))
                    continue

                val totalQuantity = entry.value.totalQuantity
                // Yes, this can happen! We should figure out why this happens, but it is not a huge issue so, whatever...
                if (0L >= totalQuantity)
                    continue

                val user = loritta.lorittaShards.retrieveUserById(entry.key)

                if (user != null && !user.isBot) {
                    val builder = StringBuilder()

                    for (list in entry.value) {
                        builder.append("${(list.usersThatTriggeredTheCheck + entry.key).joinToString(" -> ")} (${list.additionalContext}): ${list.quantity} sonhos")
                        builder.append("\r\n")
                    }

                    try {
                        logger.info { "Notifying ${user.idLong} about $userId chargebacks" }

                        loritta.getOrRetrievePrivateChannelForUser(user)
                                .sendMessage(
                                        loritta.localeManager.getLocaleById("default")
                                                .getList(
                                                        "commands.receivedSonhosFromAChargedbackUser",
                                                        Emotes.LORI_CRYING,
                                                        Emotes.LORI_SMILE,
                                                        userId.toString(),
                                                        totalQuantity
                                                ).joinToString("\n")
                                )
                                .addFiles(FileUpload.fromData(builder.toString().toByteArray(Charsets.UTF_8), "transactions.txt"))
                                .await()

                        logger.info { "Successfully notified ${user.idLong} about $userId chargebacks" }
                    } catch (e: Exception) {
                        logger.warn(e) { "Exception while trying to notify ${user.idLong} about $userId chargebacks" }
                    }
                }
            }
        }

        return triggeredSonhos
    }

    /**
     * Retrieves all sonhos that should be removed due to a chargeback made by [userId] with value [quantity]
     *
     * Values will be retrieved from the user's self account but, if the user doesn't has enough sonhos to cover the debt, recent transactions
     * will be checked too.
     *
     * @param loritta                      the loritta instance
     * @param userId                       the user ID that should be checked
     * @param quantity                     the quantity of the chargeback
     * @param quantityToBeRemovedFromUsers a map that holds all the user IDs and sonhos that should be removed
     * @param usersThatTriggeredTheCheck   a list containing all the user IDs that triggered the chargeback check
     * @param additionalContext            additional context for the check, used in the [SonhosRemovalData]
     * @return how many sonhos were successfully removed
     */
    suspend fun retrieveSonhosRemovalDueToChargeback(
            loritta: LorittaBot,
            userId: Long,
            quantity: Long,
            quantityToBeRemovedFromUsers: MutableMap<Long, MutableList<SonhosRemovalData>>,
            usersThatTriggeredTheCheck: List<Long>,
            additionalContext: String
    ): Long {
        // We lock in a mutex because a lot of times users spam chargebacks, which could cause issues when taking out money
        // mutex.withLock {
        var stillNeedsToBeRemovedSonhos = quantity

        // First we are going to try removing from the user that caused the chargeback
        val userProfile = loritta.getOrCreateLorittaProfile(userId)
        // We need to remove the sonhos already checked, because we aren't removing sonhos yet, we could charging users more than they have!
        val userMoney = (userProfile.money - quantityToBeRemovedFromUsers[userId].totalQuantity)

        logger.info { "Charging back $userId, currently they have $userMoney sonhos and we need to remove $stillNeedsToBeRemovedSonhos. Users that triggered the check: $usersThatTriggeredTheCheck" }

        val userSonhosEndResult = userMoney - stillNeedsToBeRemovedSonhos

        if (userMoney > 0) {
            if (0 > userSonhosEndResult) {
                // Oh no... we still have a looooong way to go because the user didn't have enough sonhos... sad
                // We know that the user paid his entire bank account, so we are going to subtract the "userMoney" variable
                quantityToBeRemovedFromUsers[userId] = (quantityToBeRemovedFromUsers[userId] ?: mutableListOf())
                        .also {
                            it.add(
                                    SonhosRemovalData(
                                            additionalContext,
                                            usersThatTriggeredTheCheck,
                                            userMoney
                                    )
                            )
                        }

                stillNeedsToBeRemovedSonhos -= userMoney

                logger.warn { "Charged back $userId but we are still in debt! We still need to remove $stillNeedsToBeRemovedSonhos sonhos. Users that triggered the check: $usersThatTriggeredTheCheck" }
            } else {
                quantityToBeRemovedFromUsers[userId] = (quantityToBeRemovedFromUsers[userId] ?: mutableListOf())
                        .also {
                            it.add(
                                    SonhosRemovalData(
                                            additionalContext,
                                            usersThatTriggeredTheCheck,
                                            stillNeedsToBeRemovedSonhos
                                    )
                            )
                        }

                // The user that bought the sonhos had everything fine and ok! :3
                stillNeedsToBeRemovedSonhos = 0

                logger.info { "Charged back $userId and we were able to cover all the debt from them, yay! Users that triggered the check: $usersThatTriggeredTheCheck" }
            }
        } else {
            logger.warn { "Tried charging back $userId but they don't have any sonhos! That's not good... Users that triggered the check: $usersThatTriggeredTheCheck" }
        }

        if (stillNeedsToBeRemovedSonhos > 0) {
            logger.warn { "We still need to cover $userId's debt of $stillNeedsToBeRemovedSonhos sonhos, we will start pestering other users to get the debt from them! Users that triggered the check: $usersThatTriggeredTheCheck" }

            // frick, we still have more money to be taken care of, so we need to pull the money from external sonhos transactions
            val transactions = loritta.newSuspendedTransaction {
                SonhosTransaction
                    .selectAll()
                    .where {
                    // We can't add the user that triggered the checks in our list, because if we did... We would get stuck in a infinite loop of "I want to get the debt, so let's check from this user" and then the reverse, and stuff would go on and on and on...
                    // It wouldn't work!
                    SonhosTransaction.givenBy eq userId and (SonhosTransaction.receivedBy notInList usersThatTriggeredTheCheck) and (SonhosTransaction.receivedBy.isNotNull())
                }.orderBy(SonhosTransaction.id, SortOrder.DESC)
                        .toList()
            }

            for (transaction in transactions) {
                // We already fullfilled what we needed to do, kthxbye!
                if (0 >= stillNeedsToBeRemovedSonhos)
                    break

                // This should never happen! receivedBy should always be non-null
                val givenBy = transaction[SonhosTransaction.givenBy] ?: continue
                val receivedBy = transaction[SonhosTransaction.receivedBy] ?: continue
                val receivedQuantity = transaction[SonhosTransaction.quantity]
                        .toLong()

                val howMuchNeedsToBeRemoved = Math.min(receivedQuantity, stillNeedsToBeRemovedSonhos)

                // This is harder than it looks because we don't want to just push the user's debt to someone else, we want to only charge what's needed
                val howMuchWeWereAbleToGet = retrieveSonhosRemovalDueToChargeback(
                        loritta,
                        receivedBy,
                        howMuchNeedsToBeRemoved,
                        quantityToBeRemovedFromUsers,
                        // We are going to clone the current list and add the current "givenBy" user
                        usersThatTriggeredTheCheck + givenBy,
                        "Transaction ID: ${transaction[SonhosTransaction.id]}; Reason: ${transaction[SonhosTransaction.reason]}"
                )

                stillNeedsToBeRemovedSonhos -= (receivedQuantity - howMuchWeWereAbleToGet)
                logger.info { "We were able to get $howMuchWeWereAbleToGet sonhos from $receivedBy! We still need to remove $stillNeedsToBeRemovedSonhos. Users that triggered the check: $usersThatTriggeredTheCheck" }
            }
        }

        return stillNeedsToBeRemovedSonhos
    }

    data class SonhosRemovalData(
            val additionalContext: String,
            val usersThatTriggeredTheCheck: List<Long>,
            val quantity: Long
    )

    class EconomyDisabledException : RuntimeException()
}