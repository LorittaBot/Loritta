package net.perfectdreams.cinnamon.pudding.services

import kotlinx.coroutines.runBlocking
import net.perfectdreams.cinnamon.pudding.PuddingTestUtils
import net.perfectdreams.loritta.cinnamon.pudding.services.BetsService
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.simpletransactions.SimpleSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentGateway
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers(disabledWithoutDocker = true)
class BetsServiceTest {
    @Container
    @JvmField
    val postgres = PostgreSQLContainer("postgres:14")

    @Test
    fun `test coinflip bet global`() {
        val pudding = PuddingTestUtils.createPostgreSQLPudding(postgres)

        runBlocking {
            pudding.createMissingTablesAndColumns { true }

            val user1Id = UserId(1L)
            val user2Id = UserId(2L)

            // Create both users
            pudding.users.getOrCreateUserProfile(user1Id)
            pudding.users.getOrCreateUserProfile(user2Id)

            // Update users to have 100 sonhos
            pudding.transaction {
                Profiles.update {
                    it[Profiles.money] = 100
                }
            }

            // User 1 joined the queue
            val resultUser1 = pudding.bets.addToCoinFlipBetGlobalMatchmakingQueue(
                user1Id,
                "dummy",
                "dummy",
                100L
            )

            // User 2 joined the queue
            val resultUser2 = pudding.bets.addToCoinFlipBetGlobalMatchmakingQueue(
                user2Id,
                "dummy",
                "dummy",
                100L
            )

            require(resultUser1.any { it is BetsService.AddedToQueueResult }) { "There isn't a AddedToQueueResult in resultUser1's result list!" }
            require(resultUser2.any { it is BetsService.CoinFlipResult }) { "There isn't a CoinFlipResult in resultUser2's result list!" }
            require(resultUser2.any { it is BetsService.OtherUserAchievementResult }) { "There isn't a OtherUserAchievementResult in resultUser2's result list!" }
            require(resultUser2.any { it is BetsService.SelfUserAchievementResult }) { "There isn't a SelfUserAchievementResult in resultUser2's result list!" }

            // At this point, a match should have happened, let's check!
            pudding.transaction {
                val matchesCount = CoinFlipBetGlobalMatchmakingResults.selectAll().count()
                val sonhosTransactionsCount = SimpleSonhosTransactionsLog.selectAll().where { SimpleSonhosTransactionsLog.type eq TransactionType.COINFLIP_BET_GLOBAL }.count()

                require(matchesCount == 1L) { "There isn't a matchmaking result in the CoinFlipBetGlobalMatchmakingResults table!" }
                require(sonhosTransactionsCount == 2L) { "There isn't two sonhos transactions (for $user1Id and $user2Id) in the SonhosTransactionsLog table!" }

                // Also validate the matchmaking result
                val matchmakingResult = CoinFlipBetGlobalMatchmakingResults.selectAll().limit(1).first()
                require(matchmakingResult[CoinFlipBetGlobalMatchmakingResults.quantityAfterTax] == 95L) { "Quantity After Tax should be 95 due to taxes but it is ${matchmakingResult[CoinFlipBetMatchmakingResults.quantityAfterTax]}!" }
                require(matchmakingResult[CoinFlipBetGlobalMatchmakingResults.taxPercentage] == UserPremiumPlans.Free.coinFlipRewardTax) { "Tax percentage should be ${UserPremiumPlans.Free.coinFlipRewardTax} but it is ${matchmakingResult[CoinFlipBetMatchmakingResults.taxPercentage]}!" }
            }
        }
    }

    @Test
    fun `test premium plan coinflip bet global taxes`() {
        val pudding = PuddingTestUtils.createPostgreSQLPudding(postgres)

        runBlocking {
            pudding.createMissingTablesAndColumns { true }

            val user1Id = UserId(1L)
            val user2Id = UserId(2L)

            // Create both users
            pudding.users.getOrCreateUserProfile(user1Id)
            pudding.users.getOrCreateUserProfile(user2Id)

            // Update users to have 100 sonhos and to be premium users
            pudding.transaction {
                Profiles.update {
                    it[Profiles.money] = 100
                }

                Payments.insert {
                    it[Payments.userId] = user1Id.value.toLong()
                    it[Payments.createdAt] = System.currentTimeMillis()
                    it[Payments.paidAt] = System.currentTimeMillis()
                    it[Payments.expiresAt] = Long.MAX_VALUE
                    it[Payments.gateway] = PaymentGateway.PERFECTPAYMENTS
                    it[Payments.money] = UserPremiumPlans.Complete.cost.toBigDecimal()
                    it[Payments.reason] = PaymentReason.DONATION
                }

                Payments.insert {
                    it[Payments.userId] = user2Id.value.toLong()
                    it[Payments.createdAt] = System.currentTimeMillis()
                    it[Payments.paidAt] = System.currentTimeMillis()
                    it[Payments.expiresAt] = Long.MAX_VALUE
                    it[Payments.gateway] = PaymentGateway.PERFECTPAYMENTS
                    it[Payments.money] = UserPremiumPlans.Complete.cost.toBigDecimal()
                    it[Payments.reason] = PaymentReason.DONATION
                }
            }

            // User 1 joined the queue
            pudding.bets.addToCoinFlipBetGlobalMatchmakingQueue(
                user1Id,
                "dummy",
                "dummy",
                100L
            )

            // User 2 joined the queue
            pudding.bets.addToCoinFlipBetGlobalMatchmakingQueue(
                user2Id,
                "dummy",
                "dummy",
                100L
            )

            // At this point, a match should have happened, let's check!
            pudding.transaction {
                val matchmakingResult = CoinFlipBetGlobalMatchmakingResults.selectAll().limit(1).first()
                require(matchmakingResult[CoinFlipBetGlobalMatchmakingResults.quantityAfterTax] == 100L) { "Quantity After Tax should be 100 but it is ${matchmakingResult[CoinFlipBetMatchmakingResults.quantityAfterTax]}!" }
                require(matchmakingResult[CoinFlipBetGlobalMatchmakingResults.taxPercentage] == 0.0) { "Tax percentage should be 0.0 but it is ${matchmakingResult[CoinFlipBetGlobalMatchmakingResults.taxPercentage]}!" }
            }
        }
    }

    @Test
    fun `test betting in coinflip bet global without sonhos`() {
        val pudding = PuddingTestUtils.createPostgreSQLPudding(postgres)

        runBlocking {
            pudding.createMissingTablesAndColumns { true }

            val user1Id = UserId(1L)

            // Create user
            pudding.users.getOrCreateUserProfile(user1Id)

            // User 1 joined the queue
            val result = pudding.bets.addToCoinFlipBetGlobalMatchmakingQueue(
                user1Id,
                "dummy",
                "dummy",
                100L
            )

            require(result.size == 1) { "There are more results than expected!" }
            require(result.first() is BetsService.YouDontHaveEnoughSonhosToBetResult) { "Missing YouDontHaveEnoughSonhosToBetResult from the results!" }
        }
    }
}