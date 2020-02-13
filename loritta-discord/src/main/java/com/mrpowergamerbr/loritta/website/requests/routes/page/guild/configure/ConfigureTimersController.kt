package com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.dao.Timer
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Timers
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.website.*
import kotlinx.html.div
import kotlinx.html.stream.appendHTML
import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/guild/:guildId/configure/timers")
class ConfigureTimersController {
	@GET
	@LoriRequiresAuth(LoriAuthLevel.DISCORD_GUILD_AUTH)
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>): String {
		variables["saveType"] = "timers"

		val timers = transaction(Databases.loritta) {
			Timer.find { Timers.guildId eq (variables["guild"] as Guild).idLong }.toMutableList()
		}

		val array = jsonArray()
		for (timer in timers) {
			val jsonObject = jsonObject(
					"timerId" to timer.id.value,
					"guildId" to timer.guildId,
					"channelId" to timer.channelId,
					"startsAt" to timer.startsAt,
					"repeatCount" to timer.repeatCount,
					"repeatDelay" to timer.repeatDelay,
					"effects" to gson.toJsonTree(timer.effects)
			)
			array.add(jsonObject)
		}

		println(gson.toJson(array))

		variables["timers_json"] = gson.toJson(array)

		val result = evaluateKotlin("configure_timers.kts", "onLoad", variables)
		val builder = StringBuilder()
		builder.appendHTML().div { result.invoke(this) }

		variables["timers_html"] = builder.toString()

		return evaluate("configure_timers.html", variables)
	}
}