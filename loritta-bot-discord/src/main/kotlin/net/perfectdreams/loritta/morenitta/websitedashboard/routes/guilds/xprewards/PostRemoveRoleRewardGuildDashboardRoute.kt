package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards

import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme

class PostRemoveRoleRewardGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-rewards/remove") {
    @Serializable
    data class DeleteRoleRewardRequest(
        val index: Int,
        val roles: List<RoleReward>
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val request = Json.decodeFromString<DeleteRoleRewardRequest>(call.receiveText())

        val newRoles = request.roles.toMutableList()
        newRoles.removeAt(request.index)

        call.respondHtmlFragment {
            configurableRoleRewards(
                i18nContext,
                guild,
                newRoles
            )
        }
    }
}