package net.perfectdreams.loritta.morenitta.utils

import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.tables.Payments
import net.perfectdreams.loritta.morenitta.tables.Sponsors
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object SponsorManager {
    fun retrieveActiveSponsorsFromDatabase(loritta: LorittaBot): List<Sponsor> {
        val activeSponsors = runBlocking {
            loritta.pudding.transaction {
                getActiveSponsors()
            }
        }

        return activeSponsors.map {
            Sponsor(
                it[Sponsors.name],
                it[Sponsors.slug],
                it[Payments.money],
                it[Sponsors.link],
                it[Sponsors.banners]
            )
        }
    }

    fun getActiveSponsors(): List<ResultRow> {
        return (Sponsors innerJoin Payments)
            .select {
                Payments.expiresAt greaterEq System.currentTimeMillis() and (Sponsors.enabled eq true)
            }
            .orderBy(Payments.money, SortOrder.DESC)
            .toMutableList()
    }
}