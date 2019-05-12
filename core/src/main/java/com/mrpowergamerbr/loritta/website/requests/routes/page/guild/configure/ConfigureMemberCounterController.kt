package com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure

import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.userdata.TextChannelConfig
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/guild/:guildId/configure/member-counter")
class ConfigureMemberCounterController {
	@GET
	@LoriRequiresAuth(LoriAuthLevel.DISCORD_GUILD_AUTH)
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>): String {
		variables["saveType"] = "member_counter"
		val guild = variables["guild"] as Guild
		val serverConfig = variables["serverConfig"] as MongoServerConfig

		val textChannelConfigs = mutableMapOf<TextChannel, TextChannelConfig>()

		for (textChannel in guild.textChannels) {
			val textChannelConfig = serverConfig.getTextChannelConfig(textChannel.id)
			textChannelConfigs[textChannel] = textChannelConfig
		}

		variables["textChannelConfigs"] = textChannelConfigs

		return evaluate("configure_member_counter.html", variables)
	}
}