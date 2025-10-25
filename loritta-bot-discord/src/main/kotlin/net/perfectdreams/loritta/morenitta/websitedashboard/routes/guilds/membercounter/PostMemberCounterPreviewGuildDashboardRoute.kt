package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.membercounter

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.stream.appendHTML
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.MemberCounterChannelConfigs
import net.perfectdreams.loritta.common.utils.CounterThemes
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.memberCounterPreview
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import kotlin.RuntimeException
import kotlin.UnsupportedOperationException
import kotlin.text.StringBuilder

class PostMemberCounterPreviewGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/member-counter/preview") {
    @Serializable
    data class CounterPreviewRequest(
        val theme: CounterThemes,
        val padding: Int
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val request = Json.decodeFromString<CounterPreviewRequest>(call.receiveText())

        if (request.padding !in 1..10)
            error("Too much padding! Padding must be between 1 and 10, but was ${request.padding}")

        call.respondHtml(
            createHTML(false)
                .body {
                    val counts = setOf(5, 10, 250, guild.memberCount, 1234567890).sorted()
                    for (count in counts) {
                        memberCounterPreview(count, request.theme, request.padding)
                    }
                }
        )
    }
}