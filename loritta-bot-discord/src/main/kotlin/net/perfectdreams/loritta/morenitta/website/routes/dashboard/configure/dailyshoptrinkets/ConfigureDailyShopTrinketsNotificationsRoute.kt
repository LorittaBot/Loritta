package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.dailyshoptrinkets

import io.ktor.server.application.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.LorittaDailyShopNotificationsConfigs
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.dailyshoptrinkets.GuildDailyShopTrinketsNotificationsView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.GuildDailyShopTrinketsNotificationsConfig
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.selectAll

class ConfigureDailyShopTrinketsNotificationsRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/daily-shop-trinkets") {
    override suspend fun onDashboardGuildAuthenticatedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        i18nContext: I18nContext,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification,
        guild: Guild,
        serverConfig: ServerConfig,
        colorTheme: ColorTheme
    ) {
        val databaseConfig = loritta.transaction {
            LorittaDailyShopNotificationsConfigs.selectAll()
                .where {
                    LorittaDailyShopNotificationsConfigs.id eq guild.idLong
                }
                .firstOrNull()
        }

		val config = if (databaseConfig != null) {
			GuildDailyShopTrinketsNotificationsConfig(
                databaseConfig[LorittaDailyShopNotificationsConfigs.notifyShopTrinkets],
                databaseConfig[LorittaDailyShopNotificationsConfigs.shopTrinketsChannelId],
                databaseConfig[LorittaDailyShopNotificationsConfigs.shopTrinketsMessage],

				databaseConfig[LorittaDailyShopNotificationsConfigs.notifyNewTrinkets],
				databaseConfig[LorittaDailyShopNotificationsConfigs.newTrinketsChannelId],
				databaseConfig[LorittaDailyShopNotificationsConfigs.newTrinketsMessage],
			)
		} else {
			GuildDailyShopTrinketsNotificationsConfig(
                false,
                null,
                GuildDailyShopTrinketsNotificationsView.defaultShopTrinketsTemplate.content,

				false,
				null,
				GuildDailyShopTrinketsNotificationsView.defaultNewTrinketsTemplate.content
			)
		}

        call.respondHtml(
            GuildDailyShopTrinketsNotificationsView(
                loritta.newWebsite!!,
                i18nContext,
                locale,
                getPathWithoutLocale(call),
                loritta.getLegacyLocaleById(locale.id),
                userIdentification,
                UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
                colorTheme,
                guild,
                "daily_shop_trinkets",
                config
            ).generateHtml()
        )
    }
}