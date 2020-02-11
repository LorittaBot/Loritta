package com.mrpowergamerbr.loritta.website.requests.routes.page.guild

import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/guild/:guildId/shop")
class GuildShopController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>): String {
		return evaluate("guild_shop.html", variables)
	}
}