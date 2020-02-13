package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.network.Databases
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.tables.Sponsors
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object SponsorManager {
	fun retrieveActiveSponsorsFromDatabase(): List<Sponsor> {
		val activeSponsors = transaction(Databases.loritta) {
			getActiveSponsors()
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