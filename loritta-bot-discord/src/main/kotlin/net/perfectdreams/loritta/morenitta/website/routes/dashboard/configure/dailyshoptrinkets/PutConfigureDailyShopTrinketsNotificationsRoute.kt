package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.dailyshoptrinkets

import io.ktor.server.application.*
import io.ktor.server.request.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.LorittaDailyShopNotificationsConfigs
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.dailyshoptrinkets.GuildDailyShopTrinketsNotificationsView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.config.GuildDailyShopTrinketsNotificationsConfig
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.upsert

class PutConfigureDailyShopTrinketsNotificationsRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/daily-shop-trinkets") {
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
        val params = call.receiveParameters()

        val result = loritta.newSuspendedTransaction {
            LorittaDailyShopNotificationsConfigs.upsert(LorittaDailyShopNotificationsConfigs.id) {
                it[LorittaDailyShopNotificationsConfigs.id] = guild.idLong
                it[LorittaDailyShopNotificationsConfigs.notifyShopTrinkets] = params["notifyShopTrinkets"] == "on"
                it[LorittaDailyShopNotificationsConfigs.shopTrinketsChannelId] = params["shopTrinketsChannelId"]?.toLong()
                it[LorittaDailyShopNotificationsConfigs.shopTrinketsMessage] = params["shopTrinketsMessage"]

                it[LorittaDailyShopNotificationsConfigs.notifyNewTrinkets] = params["notifyNewTrinkets"] == "on"
                it[LorittaDailyShopNotificationsConfigs.newTrinketsChannelId] = params["newTrinketsChannelId"]?.toLong()
                it[LorittaDailyShopNotificationsConfigs.newTrinketsMessage] = params["newTrinketsMessage"]
            }
        }

        val config = GuildDailyShopTrinketsNotificationsConfig(
            result[LorittaDailyShopNotificationsConfigs.notifyShopTrinkets],
            result[LorittaDailyShopNotificationsConfigs.shopTrinketsChannelId],
            result[LorittaDailyShopNotificationsConfigs.shopTrinketsMessage],

            result[LorittaDailyShopNotificationsConfigs.notifyNewTrinkets],
            result[LorittaDailyShopNotificationsConfigs.newTrinketsChannelId],
            result[LorittaDailyShopNotificationsConfigs.newTrinketsMessage],
        )

        call.response.headerHXTrigger {
            playSoundEffect = "config-saved"
            showSpicyToast(EmbeddedSpicyToast.Type.SUCCESS, "Configuração salva!")
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