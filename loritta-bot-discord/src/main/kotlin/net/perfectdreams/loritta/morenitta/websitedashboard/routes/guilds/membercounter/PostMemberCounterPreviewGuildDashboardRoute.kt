package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.membercounter

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.CounterThemes
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.memberCounterPreview
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings

class PostMemberCounterPreviewGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/member-counter/preview") {
    @Serializable
    data class CounterPreviewRequest(
        val theme: CounterThemes,
        val padding: Int
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<CounterPreviewRequest>(call.receiveText())

        if (request.padding !in 1..10)
            error("Too much padding! Padding must be between 1 and 10, but was ${request.padding}")

        call.respondHtmlFragment {
            val counts = setOf(5, 10, 250, guild.memberCount, 1234567890).sorted()
            for (count in counts) {
                memberCounterPreview(count, request.theme, request.padding)
            }
        }
    }
}