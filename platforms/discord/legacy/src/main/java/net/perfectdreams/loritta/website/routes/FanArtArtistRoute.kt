package net.perfectdreams.loritta.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.RouteKey
import net.perfectdreams.loritta.website.utils.extensions.respondHtml

class FanArtArtistRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/fanarts/{artist}") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val queryArtistId = call.parameters["artist"]

		val fanArtist = com.mrpowergamerbr.loritta.utils.loritta.fanArtArtists.firstOrNull { it.id == queryArtistId } ?: return
		val discordId = (fanArtist.socialNetworks?.firstOrNull { it.type == "discord" } as net.perfectdreams.loritta.utils.config.FanArtArtist.SocialNetwork.DiscordSocialNetwork?)?.id
		val user = discordId?.let { lorittaShards.getUserById(it)?.let { JDAUser(it) } }

		call.respondHtml(
			LorittaWebsite.INSTANCE.pageProvider.render(
				RouteKey.FAN_ART_ARTIST,
				listOf(
					getPathWithoutLocale(call),
					locale,
					fanArtist,
					user
				)
			)
		)
	}
}