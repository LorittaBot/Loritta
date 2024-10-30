package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.reactionevents

import io.ktor.server.application.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ReactionEventsConfigs
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEventsAttributes
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.lorittaevents.GuildReactionEventsView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.GuildReactionEventsConfig
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ConfigureReactionEventsRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/reaction-events") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val result = loritta.transaction {
			ReactionEventsConfigs.selectAll()
				.where {
					ReactionEventsConfigs.id eq guild.idLong
				}
				.firstOrNull()
		}

		val guildReactionEventsConfig = if (result != null) {
			GuildReactionEventsConfig(result[ReactionEventsConfigs.enabled])
		} else {
			GuildReactionEventsConfig(true)
		}

		call.respondHtml(
			GuildReactionEventsView(
				loritta.newWebsite!!,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				colorTheme,
				guild,
				ReactionEventsAttributes.getActiveEvent(Instant.now()),
				guildReactionEventsConfig
			).generateHtml()
		)
	}
}