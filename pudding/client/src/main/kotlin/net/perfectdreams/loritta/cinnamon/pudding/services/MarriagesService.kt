package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingMarriage
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarrySonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.update

class MarriagesService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getMarriage(id: Long): PuddingMarriage? {
        return pudding.transaction {
            Marriages.select { Marriages.id eq id }
                .firstOrNull()
        }?.let { PuddingMarriage.fromRow(it) }
    }

    suspend fun getMarriageByUser(user: UserId): PuddingMarriage? {
        val userId = user.value.toLong()

        return pudding.transaction {
            Marriages.select { Marriages.user1 eq userId or (Marriages.user2 eq userId) }
                .firstOrNull()
        }?.let { PuddingMarriage.fromRow(it) }
    }

    suspend fun marriageDivorceAndDelete(id: Long) = pudding.transaction {
        Profiles.update({ Profiles.marriage eq id }) {
            it[marriage] = null
        }

        Marriages.deleteWhere { Marriages.id eq id }
    }

    fun createMarriage(user1: UserId, user2: UserId): InsertStatement<Number> {
        val user1Id = user1.value.toLong()
        val user2Id = user2.value.toLong()

        return Marriages.insert {
                it[Marriages.user1] = user1Id
                it[Marriages.user2] = user2Id
                it[Marriages.marriedSince] = System.currentTimeMillis()
            }
    }

    suspend fun marry(userProfile: PuddingUserProfile, partnerProfile: PuddingUserProfile, marriageCost: Long) {
        pudding.transaction {
            val marriage = createMarriage(userProfile.id, partnerProfile.id)[Marriages.id]

            Profiles.update({ Profiles.id eq userProfile.id.value.toLong() }) {
                it[Profiles.marriage] = marriage
                it[Profiles.money] = userProfile.money - marriageCost
            }

            Profiles.update({ Profiles.id eq partnerProfile.id.value.toLong() }) {
                it[Profiles.marriage] = marriage
                it[Profiles.money] = partnerProfile.money - marriageCost
            }

            val user = Profiles.select { Profiles.id eq userProfile.id.value.toLong() }.first()
            val partner = Profiles.select { Profiles.id eq partnerProfile.id.value.toLong() }.first()

            val instant = Clock.System.now().toJavaInstant()

            MarrySonhosTransactionsLog.insert {
                it[MarrySonhosTransactionsLog.user] = user[Profiles.id]
                it[MarrySonhosTransactionsLog.partner] = partner[Profiles.id]
                it[MarrySonhosTransactionsLog.sonhos] = marriageCost
                it[MarrySonhosTransactionsLog.timestamp] = instant
            }

            MarrySonhosTransactionsLog.insert {
                it[MarrySonhosTransactionsLog.user] = partner[Profiles.id]
                it[MarrySonhosTransactionsLog.partner] = user[Profiles.id]
                it[MarrySonhosTransactionsLog.sonhos] = marriageCost
                it[MarrySonhosTransactionsLog.timestamp] = instant
            }
        }
    }
}