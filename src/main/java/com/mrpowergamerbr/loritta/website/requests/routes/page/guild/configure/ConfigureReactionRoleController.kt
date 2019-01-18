package com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure

import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
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
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>): String {
		/* variables["saveType"] = "timers"
		val guild = variables["guild"] as Guild
		val guildJson = WebsiteUtils.getGuildAsJson(guild)

		val reactionRoles = transaction(Databases.loritta) {
			ReactionOption.find {
				ReactionOptions.guildId eq guild.idLong
			}.toMutableList()
		}

		val array = jsonArray()
		for (reactionRole in reactionRoles) {
			val jsonObject = jsonObject(
					"id" to reactionRole.id,
					"guildId" to reactionRole.guildId,
					"textChannelId" to reactionRole.textChannelId,
					"messageId" to reactionRole.messageId,
					"reaction" to reactionRole.reaction,
					"locks" to reactionRole.locks,
					"roleIds" to reactionRole.roleIds
			)
			array.add(jsonObject)
		}

		guildJson["reactionRoles"] to array

		variables["reaction_role_json"] = gson.toJson(guildJson)

		val result = evaluateKotlin("configure_reaction_role.kts", "onLoad", variables)
		val builder = StringBuilder()
		builder.appendHTML().div { result.invoke(this) }

		variables["reaction_role_html"] = builder.toString() */

		return evaluate("configure_reaction_role.html", variables)
	}
}