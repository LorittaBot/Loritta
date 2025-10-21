package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.warnactions

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableWarnList
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.ColorTheme

class PostAddWarnActionGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/warn-actions/add") {
    @Serializable
    data class AddWarnActionRequest(
        val count: Int,
        val action: PunishmentAction,
        val time: String?,
        val actions: List<WarnAction> = listOf()
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val request = Json.decodeFromString<AddWarnActionRequest>(call.receiveText())

        val newWarns = request.actions.toMutableList()
        newWarns.add(
            WarnAction(
                request.count,
                request.action,
                if (request.action == PunishmentAction.MUTE)
                    request.time
                else null
            )
        )

        call.respondHtml(
            createHTML(false)
                .body {
                    configurableWarnList(
                        i18nContext,
                        guild,
                        newWarns
                    )
                }
        )
    }
}