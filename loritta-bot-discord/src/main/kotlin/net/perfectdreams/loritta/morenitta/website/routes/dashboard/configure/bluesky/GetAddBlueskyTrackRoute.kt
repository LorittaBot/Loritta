package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
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

class GetAddBlueskyTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/bluesky/add") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		// "Handles are not case-sensitive, which means they can be safely normalized from user input to lower-case (ASCII) form."
		//https://atproto.com/specs/handle
		val handle = call.parameters
			.getOrFail("handle")
			.removePrefix("@")
			.lowercase()

		val http = loritta.http.get("https://public.api.bsky.app/xrpc/app.bsky.actor.getProfile") {
			parameter("actor", handle)
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
								EmbeddedSpicyToast(
									EmbeddedSpicyToast.Type.WARN, "Conta não existe!", null)
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

		if (call.request.header("HX-Request")?.toBoolean() == true) {
			call.response.header(
				"HX-Trigger",
				buildJsonObject {
					put("closeSpicyModal", null)
					put("playSoundEffect", "config-saved")
				}.toString()
			)
		}

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
				null,
				profile,
				GuildConfigureBlueskyProfileView.BlueskyTrackSettings(
					null,
					"Nova postagem {post.url}"
				)
			).generateHtml()
		)
	}
}