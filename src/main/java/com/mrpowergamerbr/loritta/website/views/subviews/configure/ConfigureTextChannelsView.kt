package com.mrpowergamerbr.loritta.website.views.subviews.configure

import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.userdata.TextChannelConfig
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import org.jooby.Request
import org.jooby.Response
import kotlin.collections.set

class ConfigureTextChannelsView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path.matches(Regex("^/dashboard/configure/[0-9]+/textchannels"))
	}

	override fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: MongoServerConfig): String {
		variables["saveType"] = "text_channels"
		val textChannelConfigs = mutableMapOf<TextChannel, TextChannelConfig>()

		for (textChannel in guild.textChannels) {
			val textChannelConfig = serverConfig.getTextChannelConfig(textChannel.id)
			textChannelConfigs[textChannel] = textChannelConfig
		}

		variables["textChannelConfigs"] = textChannelConfigs
		return evaluate("configure_textchannels.html", variables)
	}
}