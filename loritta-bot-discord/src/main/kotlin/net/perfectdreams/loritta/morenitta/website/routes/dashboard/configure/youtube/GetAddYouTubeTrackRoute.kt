package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.respondBodyAsHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.youtube.GuildConfigureYouTubeChannelView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class GetAddYouTubeTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/youtube/add") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val channelLink = call.parameters.getOrFail("channelLink")

		val result = YouTubeWebUtils.getYouTubeChannelInfoFromURL(loritta, channelLink)

		when (result) {
			is YouTubeWebUtils.YouTubeChannelInfoResult.Success -> {
				if (call.request.header("HX-Request")?.toBoolean() == true) {
					call.response.headerHXTrigger {
						closeSpicyModal = true
						playSoundEffect = "config-saved"
					}
				}

				call.respondHtml(
					GuildConfigureYouTubeChannelView(
						loritta.newWebsite!!,
						i18nContext,
						locale,
						getPathWithoutLocale(call),
						loritta.getLegacyLocaleById(locale.id),
						userIdentification,
						UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
						colorTheme,
						guild,
						null,
						YouTubeChannel(
							result.channel.channelId,
							result.channel.name,
							result.channel.avatarUrl
						),
						GuildConfigureYouTubeChannelView.YouTubeTrackSettings(
							null,
							"Novo vídeo no canal! {video.url}"
						)
					).generateHtml()
				)
			}
			is YouTubeWebUtils.YouTubeChannelInfoResult.Error -> {
				call.respondBodyAsHXTrigger(status = HttpStatusCode.InternalServerError) {
					playSoundEffect = "config-error"
					showSpicyToast(
						EmbeddedSpicyToast.Type.WARN,
						"Algo deu errado ao tentar pegar as informações do canal!",
					)
				}
			}
			YouTubeWebUtils.YouTubeChannelInfoResult.InvalidUrl -> {
				call.respondBodyAsHXTrigger(status = HttpStatusCode.BadRequest) {
					playSoundEffect = "config-error"
					showSpicyToast(
						EmbeddedSpicyToast.Type.WARN,
						"URL inválida!"
					)
				}
			}
			YouTubeWebUtils.YouTubeChannelInfoResult.UnknownChannel -> {
				call.respondBodyAsHXTrigger(status = HttpStatusCode.BadRequest) {
					playSoundEffect = "config-error"
					showSpicyToast(
						EmbeddedSpicyToast.Type.WARN,
						"Canal não existe!",
					)
				}
			}
		}
	}
}