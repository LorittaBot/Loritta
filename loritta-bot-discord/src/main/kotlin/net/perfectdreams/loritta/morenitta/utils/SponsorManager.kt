package net.perfectdreams.loritta.morenitta.utils

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Sponsors
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.*
import java.time.Instant

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
				JsonParser.parseString(it[Sponsors.banners])
			)
		}
	}

	fun getActiveSponsors(): List<ResultRow> {
		val now = Instant.now()
		return Sponsors.selectAll()
			.where {
				Sponsors.startsAt lessEq now and (Sponsors.endsAt greaterEq now or Sponsors.endsAt.isNull()) and (Sponsors.enabled eq true)
			}
			.orderBy(Sponsors.sponsorValue, SortOrder.DESC)
			.toMutableList()
	}
}