package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.respondBodyAsHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.SpicyMorenittaTriggers
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.youtube.GuildConfigureYouTubeChannelView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class PatchYouTubeTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/youtube/tracks/{trackId}") {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val postParams = call.receiveParameters()
		val trackId = call.parameters.getOrFail("trackId").toLong()
		val youtubeChannelId = postParams.getOrFail("youtubeChannelId")
		val channelId = postParams.getOrFail("channelId").toLong()
		val message = postParams.getOrFail("message")
		val result = YouTubeWebUtils.getYouTubeChannelInfoFromChannelId(loritta, youtubeChannelId)

		when (result) {
			is YouTubeWebUtils.YouTubeChannelInfoResult.Success -> {
				val insertedRow = loritta.transaction {
					TrackedYouTubeAccounts.update({
						TrackedYouTubeAccounts.guildId eq guild.idLong and (TrackedYouTubeAccounts.id eq trackId)
					}) {
						it[TrackedYouTubeAccounts.youTubeChannelId] = youtubeChannelId
						it[TrackedYouTubeAccounts.guildId] = guild.idLong
						it[TrackedYouTubeAccounts.channelId] = postParams.getOrFail("channelId").toLong()
						it[TrackedYouTubeAccounts.message] = postParams.getOrFail("message")
						it[TrackedYouTubeAccounts.editedAt] = Instant.now()
					}

					TrackedYouTubeAccounts.selectAll()
						.where {
							TrackedYouTubeAccounts.guildId eq guild.idLong and (TrackedYouTubeAccounts.id eq trackId)
						}
						.first()
				}

				call.response.headerHXTrigger(SpicyMorenittaTriggers(playSoundEffect = "config-saved"))

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
						insertedRow[TrackedYouTubeAccounts.id].value,
						YouTubeChannel(
							result.channel.channelId,
							result.channel.name,
							result.channel.avatarUrl
						),
						GuildConfigureYouTubeChannelView.YouTubeTrackSettings(
							insertedRow[TrackedYouTubeAccounts.channelId],
							insertedRow[TrackedYouTubeAccounts.message]
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
				call.respondBodyAsHXTrigger(
					status = HttpStatusCode.BadRequest
				) {
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