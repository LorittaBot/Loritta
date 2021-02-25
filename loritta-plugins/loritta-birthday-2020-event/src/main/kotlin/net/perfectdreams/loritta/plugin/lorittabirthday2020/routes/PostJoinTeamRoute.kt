package net.perfectdreams.loritta.plugin.lorittabirthday2020.routes

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.lorittabirthday2020.LorittaBirthday2020Event
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Players
import net.perfectdreams.loritta.plugin.lorittabirthday2020.utils.BirthdayTeam
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class PostJoinTeamRoute(val m: LorittaBirthday2020Event, loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/birthday-2020/team") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		loritta as Loritta
		val team = transaction(Databases.loritta) {
			Birthday2020Players.select {
				Birthday2020Players.user eq userIdentification.id.toLong()
			}.firstOrNull()
		}

		if (team != null) {
			call.respondJson(jsonObject(), HttpStatusCode.Forbidden)
			return
		}

		val payload = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()) }

		val idLong = userIdentification.id
		val profile = loritta.getOrCreateLorittaProfile(idLong)

		transaction(Databases.loritta) {
			Birthday2020Players.insert {
				it[user] = profile.id
				it[Birthday2020Players.team] = BirthdayTeam.valueOf(payload["team"].string)
			}
		}

		call.respondJson(jsonObject())
	}
}