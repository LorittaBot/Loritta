package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils

import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.cinnamon.common.utils.DailyTaxThresholds
import net.perfectdreams.loritta.cinnamon.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentGateway
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers(disabledWithoutDocker = true)
class InactiveDailyUsersTest {
    @Container
    @JvmField
    val postgres = PostgreSQLContainer("postgres:14")

    @Test
    fun `check if the inactivity daily tax should be applied`() {
        val pudding = Pudding.createPostgreSQLPudding("${postgres.containerIpAddress}:${postgres.getMappedPort(5432)}", postgres.databaseName, postgres.username, postgres.password)

        runBlocking {
            pudding.createMissingTablesAndColumns { true }

            val nonPremiumUserId = UserId(1L)
            val essentialPlanUserId = UserId(2L)
            val completePlanUserId = UserId(3L)

            // Create users
            pudding.users.getOrCreateUserProfile(nonPremiumUserId)
            pudding.users.getOrCreateUserProfile(essentialPlanUserId)
            pudding.users.getOrCreateUserProfile(completePlanUserId)

            pudding.transactionOrUseThreadLocalTransaction {
                // Update users to have sonhos
                Profiles.update({ Profiles.id eq nonPremiumUserId.value.toLong() }) {
                    it[money] = 100000
                }

                Profiles.update({ Profiles.id eq essentialPlanUserId.value.toLong() }) {
                    it[money] = 100000
                }

                Profiles.update({ Profiles.id eq completePlanUserId.value.toLong() }) {
                    it[money] = 100000
                }

                // Essential premium user, should be included
                Payments.insert {
                    it[Payments.userId] = essentialPlanUserId.value.toLong()
                    it[Payments.createdAt] = System.currentTimeMillis()
                    it[Payments.paidAt] = System.currentTimeMillis()
                    it[Payments.expiresAt] = Long.MAX_VALUE
                    it[Payments.gateway] = PaymentGateway.PERFECTPAYMENTS
                    it[Payments.money] = UserPremiumPlans.Essential.cost.toBigDecimal()
                    it[Payments.reason] = PaymentReason.DONATION
                }

                // Complete premium user, shouldn't be included
                Payments.insert {
                    it[Payments.userId] = completePlanUserId.value.toLong()
                    it[Payments.createdAt] = System.currentTimeMillis()
                    it[Payments.paidAt] = System.currentTimeMillis()
                    it[Payments.expiresAt] = Long.MAX_VALUE
                    it[Payments.gateway] = PaymentGateway.PERFECTPAYMENTS
                    it[Payments.money] = UserPremiumPlans.Complete.cost.toBigDecimal()
                    it[Payments.reason] = PaymentReason.DONATION
                }

                val matchedUsers = mutableListOf<Pair<DailyTaxThresholds.DailyTaxThreshold, DailyTaxUtils.InactiveDailyUser>>()

                DailyTaxUtils.getAndProcessInactiveDailyUsers(
                    0L, // doesn't matter, it is Loritta's ID
                    0
                ) { threshold, tax ->
                    matchedUsers.add(Pair(threshold, tax))
                }

                require(matchedUsers.any { it.second.id == nonPremiumUserId.value.toLong() }) { "Missing nonPremiumUserId in the matchedUsers list!" }
                require(matchedUsers.any { it.second.id == essentialPlanUserId.value.toLong() }) { "Missing essentialPlanUserId in the matchedUsers list!"  }
                require(!matchedUsers.any { it.second.id == completePlanUserId.value.toLong() }) { "completePlanUserId is present in the matchedUsers list, but they shouldn't be!" }
            }
        }
    }
}