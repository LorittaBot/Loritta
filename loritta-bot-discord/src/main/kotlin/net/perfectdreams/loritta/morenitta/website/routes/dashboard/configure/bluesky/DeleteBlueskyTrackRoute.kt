package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.bluesky.GuildBlueskyView.Companion.createBlueskyAccountCards
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

class DeleteBlueskyTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/bluesky/tracks/{trackId}") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val trackId = call.parameters.getOrFail("trackId").toLong()

		val trackedBlueskyAccounts = loritta.transaction {
			TrackedBlueskyAccounts.deleteWhere {
				TrackedBlueskyAccounts.guildId eq guild.idLong and (TrackedBlueskyAccounts.id eq trackId)
			}

			TrackedBlueskyAccounts.selectAll()
				.where {
					TrackedBlueskyAccounts.guildId eq guild.idLong
				}
				.toList()
		}

		val blueskyProfiles = mutableListOf<BlueskyProfile>()
		if (trackedBlueskyAccounts.isNotEmpty()) {
			val http = loritta.http.get("https://public.api.bsky.app/xrpc/app.bsky.actor.getProfiles") {
				// The docs are wrong, this is a "array", as in, you need to specify multiple parameters
				for (trackedBlueskyAccount in trackedBlueskyAccounts.take(25)) {
					parameter("actors", trackedBlueskyAccount[TrackedBlueskyAccounts.repo])
				}
			}

			val profiles = JsonIgnoreUnknownKeys.decodeFromString<BlueskyProfiles>(http.bodyAsText(Charsets.UTF_8))
			blueskyProfiles.addAll(profiles.profiles)
		}

		call.response.header(
			"HX-Trigger",
			buildJsonObject {
				put("closeSpicyModal", null)
				put("playSoundEffect", "recycle-bin")
			}.toString()
		)
		call.respondHtml(
			createHTML()
				.body {
					createBlueskyAccountCards(
						i18nContext,
						guild,
						trackedBlueskyAccounts,
						blueskyProfiles
					)
				}
		)
	}
}