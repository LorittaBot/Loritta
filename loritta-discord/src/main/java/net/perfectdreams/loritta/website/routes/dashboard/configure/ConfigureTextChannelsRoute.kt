package net.perfectdreams.loritta.website.routes.dashboard.configure

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.TextChannelConfig
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.set

class ConfigureTextChannelsRoute(loritta: LorittaDiscord) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/text-channels") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild) {
		loritta as Loritta
		val serverConfig = loritta.getServerConfigForGuild(guild.id)

		val variables = call.legacyVariables(locale)

		variables["saveType"] = "text_channels"
		val textChannelConfigs = mutableMapOf<TextChannel, TextChannelConfig>()

		for (textChannel in guild.textChannels) {
			val textChannelConfig = serverConfig.getTextChannelConfig(textChannel.id)
			textChannelConfigs[textChannel] = textChannelConfig
		}

		variables["textChannelConfigs"] = textChannelConfigs
		call.respondHtml(evaluate("configure_textchannels.html", variables))
	}
}