package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableChannelList
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme

open class GenericAddChannelToListGuildDashboardRoute(
    website: LorittaDashboardWebServer,
    originalGuildPath: String,
    val removeEndpoint: String,
) : RequiresGuildAuthDashboardLocalizedRoute(website, originalGuildPath) {
    @Serializable
    data class AddChannelRequest(
        val channelId: Long,
        val channels: Set<Long> = setOf()
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val request = Json.decodeFromString<AddChannelRequest>(call.receiveText())

        if (request.channelId in request.channels) {
            call.respondHtml(
                createHTML(false)
                    .body {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Você já tem este canal adicionado!"
                            )
                        )
                    },
                status = HttpStatusCode.Conflict
            )
            return
        }

        val newList = request.channels.toMutableSet()
        newList.add(request.channelId)

        call.respondHtml(
            createHTML(false)
                .body {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            "Canal adicionado!"
                        )
                    )

                    configurableChannelList(
                        i18nContext,
                        guild,
                        "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}$removeEndpoint",
                        newList
                    )
                }
        )
    }
}