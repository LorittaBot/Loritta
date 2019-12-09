package net.perfectdreams.loritta.endpoints

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.Christmas2019
import net.perfectdreams.loritta.tables.CollectedChristmas2019Points
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/loritta/christmas2019")
class Christmas2019StatsController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)

		val points = transaction(Databases.loritta) {
			CollectedChristmas2019Points.selectAll().count()
		}

		val alreadyReceived = Christmas2019.alreadyReceivedDropTypes(points)
		val whatIsTheNextDrop = Christmas2019.whatIsTheNextDrop(points)

		val helpingHands = jsonObject()
		val requiredPoints = jsonObject()

		val discordUsers = mutableListOf<User>()

		for (x in Christmas2019.DropType.values().indices) {
			val dropType = Christmas2019.DropType.values()[x]
			val previousDropType = Christmas2019.DropType.values().getOrNull(x - 1)

			val previousNeeds = previousDropType?.requiredPoints ?: 0

			val helped = transaction(Databases.loritta) {
				CollectedChristmas2019Points.selectAll()
						.limit(dropType.requiredPoints - previousNeeds, previousNeeds)
						.groupBy(CollectedChristmas2019Points.id, CollectedChristmas2019Points.user)
						.toMutableList()
			}

			val users = jsonArray()

			for (user in helped.distinctBy { it[CollectedChristmas2019Points.user].value }) {
				users.add(
						user[CollectedChristmas2019Points.user].value
				)

				if (discordUsers.any { user[CollectedChristmas2019Points.user].value == it.idLong }) {
					val user = runBlocking {
						lorittaShards.retrieveUserById(
								user[CollectedChristmas2019Points.user].value
						)
					} ?: continue

					discordUsers.add(user)
				}
			}

			helpingHands[dropType.name] = users
			requiredPoints[dropType.name] = dropType.requiredPoints
		}

		val users = jsonArray()
		for (user in discordUsers) {
			users.add(
					ParallaxUtils.transformToJson(user)
			)
		}

		val jsonObject = jsonObject(
				"points" to points,
				"requiredPoints" to requiredPoints,
				"alreadyReceived" to alreadyReceived.map { it.name }.toJsonArray(),
				"nextDrop" to whatIsTheNextDrop?.name,
				"helpingHands" to helpingHands
		)

		res.send(gson.toJson(jsonObject))
	}
}