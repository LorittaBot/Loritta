package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.starboard

import dev.minn.jda.ktx.generics.getChannel
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.starboardStorytime
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme

class PostStarboardStorytimeGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/starboard/storytime") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val request = Json.decodeFromString<PutStarboardGuildDashboardRoute.SaveStarboardRequest>(call.receiveText())

        val starboardChannel = guild.getChannel(request.starboardChannelId)

        call.respondHtmlFragment {
            starboardStorytime(i18nContext, starboardChannel, request.requiredStars, website.loritta.lorittaShards.shardManager.shards.first().selfUser)
        }
    }
}