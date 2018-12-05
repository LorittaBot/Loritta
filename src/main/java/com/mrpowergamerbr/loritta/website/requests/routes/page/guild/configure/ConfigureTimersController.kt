package com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure

import com.mrpowergamerbr.loritta.website.*
import kotlinx.html.div
import kotlinx.html.stream.appendHTML
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
		val result = evaluateKotlin("configure_timers.kts", "onLoad", variables)
		val builder = StringBuilder()
		builder.appendHTML().div { result.invoke(this) }

		variables["timers_html"] = builder.toString()

		return evaluate("configure_timers.html", variables)
	}
}