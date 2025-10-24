package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.dailyshoptrinkets

import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.LorittaDailyShopNotificationsConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.upsert

class PutDailyShopTrinketsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/daily-shop-trinkets") {
    @Serializable
    data class SaveDailyShopTrinketsRequest(
        val notifyShopTrinkets: Boolean,
        val shopTrinketsChannelId: Long,
        val shopTrinketsMessage: String,

        val notifyNewTrinkets: Boolean,
        val newTrinketsChannelId: Long,
        val newTrinketsMessage: String,
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val request = Json.decodeFromString<SaveDailyShopTrinketsRequest>(call.receiveText())

        website.loritta.newSuspendedTransaction {
            LorittaDailyShopNotificationsConfigs.upsert(LorittaDailyShopNotificationsConfigs.id) {
                it[LorittaDailyShopNotificationsConfigs.id] = guild.idLong
                it[LorittaDailyShopNotificationsConfigs.notifyShopTrinkets] = request.notifyShopTrinkets
                it[LorittaDailyShopNotificationsConfigs.shopTrinketsChannelId] = request.shopTrinketsChannelId
                it[LorittaDailyShopNotificationsConfigs.shopTrinketsMessage] = request.shopTrinketsMessage

                it[LorittaDailyShopNotificationsConfigs.notifyNewTrinkets] = request.notifyNewTrinkets
                it[LorittaDailyShopNotificationsConfigs.newTrinketsChannelId] = request.newTrinketsChannelId
                it[LorittaDailyShopNotificationsConfigs.newTrinketsMessage] = request.newTrinketsMessage
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}