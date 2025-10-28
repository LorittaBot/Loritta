package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprates

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.body
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
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableRoleRates
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme

class PostAddRoleRateGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-rates/add") {
    @Serializable
    data class AddRoleRateRequest(
        val roleId: Long,
        val rate: Double,
        val roles: Set<RoleRate> = setOf(),
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val request = Json.decodeFromString<AddRoleRateRequest>(call.receiveText())

        val newRoles = request.roles.toMutableList()
        newRoles.add(
            RoleRate(
                request.roleId,
                request.rate
            )
        )

        call.respondHtmlFragment {
            configurableRoleRates(
                i18nContext,
                guild,
                newRoles
            )
        }
    }
}