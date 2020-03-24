package net.perfectdreams.loritta.plugin.lorittabirthday2020.routes

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.lorittabirthday2020.LorittaBirthday2020
import net.perfectdreams.loritta.plugin.lorittabirthday2020.LorittaBirthday2020Event
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Players
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GetCurrentTeamRoute(val m: LorittaBirthday2020Event, loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/birthday-2020/team") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		loritta as Loritta
		val team = transaction(Databases.loritta) {
			Birthday2020Players.select {
				Birthday2020Players.user eq userIdentification.id.toLong()
			}.firstOrNull()
		}

		val json = jsonObject(
				"isActive" to LorittaBirthday2020.isEventActive()
		)

		if (team == null) {
			call.respondJson(json)
			return
		}

		json["team"] = team[Birthday2020Players.team].name
		call.respondJson(json)
	}
}