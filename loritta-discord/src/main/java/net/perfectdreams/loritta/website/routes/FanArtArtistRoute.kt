package net.perfectdreams.loritta.website.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import io.ktor.request.path
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import java.io.File
import kotlin.reflect.full.createType

class FanArtArtistRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/fanarts/{artist}") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val queryArtistId = call.parameters["artist"]

		val fanArtist = com.mrpowergamerbr.loritta.utils.loritta.fanArtArtists.firstOrNull { it.id == queryArtistId } ?: return
		val discordId = (fanArtist.socialNetworks?.firstOrNull { it.type == "discord" } as net.perfectdreams.loritta.utils.config.FanArtArtist.SocialNetwork.DiscordSocialNetwork?)?.id
		val user = discordId?.let { lorittaShards.getUserById(it)?.let { JDAUser(it) } }

		val html = ScriptingUtils.evaluateWebPageFromTemplate(
					File(
							"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/fan_art_artist.kts"
					),
					mapOf(
							"path" to call.request.path().split("/").drop(2).joinToString("/"),
							"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
							"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), locale),
							"artist" to fanArtist,
							"user" to ScriptingUtils.WebsiteArgumentType(User::class.createType(nullable = true), user)
					)
			)

		call.respondHtml(html)
	}
}