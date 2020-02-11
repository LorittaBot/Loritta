package com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import net.dv8tion.jda.api.entities.Guild
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/guild/:guildId/configure/reaction-role")
class ConfigureReactionRoleController {
	@GET
	@LoriRequiresAuth(LoriAuthLevel.DISCORD_GUILD_AUTH)
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>, @Local guild: Guild, @Local newServerConfig: ServerConfig, @Local serverConfig: MongoServerConfig, @Local userIdentification: SimpleUserIdentification): String {
		variables["saveType"] = "reaction-role"
		variables["reaction_role_json"] = gson.toJson(WebsiteUtils.transformToDashboardConfigurationJson(userIdentification, guild, newServerConfig, serverConfig))

		return evaluate("configure_reaction_role.html", variables)
	}
}