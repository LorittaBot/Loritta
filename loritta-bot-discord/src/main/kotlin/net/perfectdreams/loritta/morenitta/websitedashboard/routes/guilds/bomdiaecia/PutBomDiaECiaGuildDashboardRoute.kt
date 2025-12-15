package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bomdiaecia

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.BomDiaECiaConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.upsert
import java.time.OffsetDateTime

class PutBomDiaECiaGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/bom-dia-e-cia") {
    @Serializable
    data class SaveBomDiaECiaRequest(
        val enableBomDiaECia: Boolean,
        val blockedChannels: Set<Long> = setOf(),
        val useBlockedChannelsAsAllowedChannels: Boolean
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SaveBomDiaECiaRequest>(call.receiveText())

        website.loritta.transaction {
            BomDiaECiaConfigs.upsert(BomDiaECiaConfigs.id) {
                it[BomDiaECiaConfigs.id] = guild.idLong
                it[BomDiaECiaConfigs.enabled] = request.enableBomDiaECia
                it[BomDiaECiaConfigs.blockedChannels] = request.blockedChannels.toList()
                it[BomDiaECiaConfigs.useBlockedChannelsAsAllowedChannels] = request.useBlockedChannelsAsAllowedChannels
                it[BomDiaECiaConfigs.updatedAt] = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}