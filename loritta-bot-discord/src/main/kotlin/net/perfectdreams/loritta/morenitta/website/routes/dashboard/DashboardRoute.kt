package net.perfectdreams.loritta.morenitta.website.routes.dashboard

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserFavoritedGuilds
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.SelectGuildProfileDashboardView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import kotlin.collections.set

class DashboardRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedDashboardRoute(loritta, "/dashboard") {
	override suspend fun onDashboardAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, colorTheme: ColorTheme) {
		// See: "HTTP Headers & Caching"
		// https://hypermedia.systems/more-htmx-patterns/
		// When using a trigger on a same path, we need to bust the cache based on the trigger header, to avoid the back button
		// going back to a partial view
		// YES IT NEEDS TO BE PRESENT ON ANY REQUEST THAT INVOLVES FRAGMENTS
		call.response.header("Vary", "HX-Trigger")

		val view = SelectGuildProfileDashboardView(
			loritta,
			i18nContext,
			locale,
			getPathWithoutLocale(call),
			loritta.getLegacyLocaleById(locale.id),
			userIdentification,
			UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
			colorTheme
		)

		if (call.request.header("HX-Trigger") == "user-guilds-wrapper") {
			val lorittaProfile = loritta.getOrCreateLorittaProfile(userIdentification.id.toLong())

			val userGuilds = discordAuth.getUserGuilds()
			val userGuildsIds = userGuilds.map { it.id.toLong() }

			val favoritedGuilds = loritta.newSuspendedTransaction {
				// Update if the user is in a guild or not based on the retrieved guilds
				GuildProfiles.update({ (GuildProfiles.userId eq lorittaProfile.id.value) and (GuildProfiles.guildId inList userGuildsIds) }) {
					it[GuildProfiles.isInGuild] = true
				}

				GuildProfiles.update({ (GuildProfiles.userId eq lorittaProfile.id.value) and (GuildProfiles.guildId notInList userGuildsIds) }) {
					it[GuildProfiles.isInGuild] = false
				}

				// Remove all unknown guilds from the favorite guilds list, mostly to avoid users hitting the 200 favorited guilds limit
				// We do this before querying to user's favorited guilds
				UserFavoritedGuilds.deleteWhere {
					UserFavoritedGuilds.userId eq userIdentification.id.toLong() and (UserFavoritedGuilds.guildId notInList userGuildsIds)
				}
				val favoritedGuilds = UserFavoritedGuilds.selectAll()
					.where {
						UserFavoritedGuilds.userId eq userIdentification.id.toLong()
					}
					.map { it[UserFavoritedGuilds.guildId] }
					.toSet()

				return@newSuspendedTransaction favoritedGuilds
			}

			val guilds = userGuilds.filter { LorittaWebsite.canManageGuild(it) }

			val userPermissionLevels = mutableMapOf<TemmieDiscordAuth.Guild, LorittaWebsite.UserPermissionLevel>()
			val joinedServers = mutableMapOf<TemmieDiscordAuth.Guild, Boolean>()
			for (guild in guilds) {
				userPermissionLevels[guild] = LorittaWebsite.getUserPermissionLevel(guild)
				joinedServers[guild] = loritta.lorittaShards.getGuildById(guild.id) != null
			}

			call.respondHtml(
				createHTML()
					.div {
						apply(view.userGuilds(guilds, favoritedGuilds ?: setOf()))
					}
			)
			return
		}


		call.respondHtml(view.generateHtml())
	}
}