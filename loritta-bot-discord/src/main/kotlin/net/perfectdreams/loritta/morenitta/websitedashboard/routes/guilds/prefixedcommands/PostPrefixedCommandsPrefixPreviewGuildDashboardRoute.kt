package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.prefixedcommands

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.prefixPreview
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.ColorTheme

class PostPrefixedCommandsPrefixPreviewGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/prefixed-commands/prefix-preview") {
    @Serializable
    data class PrefixPreviewRequest(val prefix: String)

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val request = Json.decodeFromString<PrefixPreviewRequest>(call.receiveText())

        call.respondHtml(
            createHTML()
                .body {
                    prefixPreview(
                        session,
                        request.prefix,
                        website.loritta.lorittaShards.shardManager.shards.first().selfUser
                    )
                }
        )
    }
}