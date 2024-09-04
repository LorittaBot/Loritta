package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube

import io.ktor.server.application.*
import io.ktor.server.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.youtube.GuildYouTubeView.Companion.createYouTubeAccountCards
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import kotlin.math.ceil

class DeleteYouTubeTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/youtube/tracks/{trackId}") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val trackId = call.parameters.getOrFail("trackId").toLong()

		val (trackedYouTubeAccounts, valueOfTheDonationKeysEnabledOnThisGuild) = loritta.transaction {
			TrackedYouTubeAccounts.deleteWhere {
				TrackedYouTubeAccounts.guildId eq guild.idLong and (TrackedYouTubeAccounts.id eq trackId)
			}

			val trackedYouTubeAccounts = TrackedYouTubeAccounts.selectAll()
				.where {
					TrackedYouTubeAccounts.guildId eq guild.idLong
				}
				.toList()

			val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
				.sumOf { it.value }
				.let { ceil(it) }

			Pair(trackedYouTubeAccounts, valueOfTheDonationKeysEnabledOnThisGuild)

		}

		val youtubeChannels = trackedYouTubeAccounts.map {
			GlobalScope.async {
				YouTubeWebUtils.getYouTubeChannelInfoFromChannelId(loritta, it[TrackedYouTubeAccounts.youTubeChannelId])
			}
		}.awaitAll().mapNotNull { (it as? YouTubeWebUtils.YouTubeChannelInfoResult.Success)?.channel }

		call.response.headerHXTrigger {
			closeSpicyModal = true
			playSoundEffect = "recycle-bin"
			spicyToast = EmbeddedSpicyModalUtils.createSpicyToast(EmbeddedSpicyToast.Type.SUCCESS, "Canal deletado!")
		}

		call.respondHtml(
			createHTML()
				.body {
					createYouTubeAccountCards(loritta, i18nContext, guild, ServerPremiumPlans.getPlanFromValue(valueOfTheDonationKeysEnabledOnThisGuild), trackedYouTubeAccounts, youtubeChannels)
				}
		)
	}
}