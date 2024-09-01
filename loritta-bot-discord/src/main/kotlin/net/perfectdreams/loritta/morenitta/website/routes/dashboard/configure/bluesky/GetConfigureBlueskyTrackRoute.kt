package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildConfigureBlueskyProfileView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class GetConfigureBlueskyTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/bluesky/tracks/{trackId}") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val trackId = call.parameters.getOrFail("trackId").toLong()

		val blueskyAccountTrack = loritta.transaction {
			TrackedBlueskyAccounts.selectAll()
				.where {
					TrackedBlueskyAccounts.guildId eq guild.idLong and (TrackedBlueskyAccounts.id eq trackId)
				}
				.firstOrNull()
		}

		if (blueskyAccountTrack == null)
			return

		val http = loritta.http.get("https://public.api.bsky.app/xrpc/app.bsky.actor.getProfile") {
			parameter("actor", blueskyAccountTrack[TrackedBlueskyAccounts.repo])
		}

		if (http.status == HttpStatusCode.BadRequest) {
			call.response.header(
				"HX-Trigger",
				buildJsonObject {
					put("playSoundEffect", "config-error")
					put(
						"showSpicyToast",
						EmbeddedSpicyModalUtils.encodeURIComponent(
							Json.encodeToString(
								EmbeddedSpicyToast(EmbeddedSpicyToast.Type.WARN, "Conta não existe!", null)
							)
						)
					)
				}.toString()
			)
			call.respondText("", status = HttpStatusCode.BadRequest)
			return
		}

		val textStuff = http.bodyAsText(Charsets.UTF_8)
		val profile = JsonIgnoreUnknownKeys.decodeFromString<BlueskyProfile>(textStuff)

		call.respondHtml(
			GuildConfigureBlueskyProfileView(
				loritta.newWebsite!!,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				colorTheme,
				guild,
				"bluesky",
				trackId,
				profile,
				GuildConfigureBlueskyProfileView.BlueskyTrackSettings(
					blueskyAccountTrack[TrackedBlueskyAccounts.channelId],
					blueskyAccountTrack[TrackedBlueskyAccounts.message]
				)
			).generateHtml()
		)
	}
}