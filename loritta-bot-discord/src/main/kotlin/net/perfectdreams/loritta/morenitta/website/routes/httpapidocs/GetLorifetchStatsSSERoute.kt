package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.SseEvent
import net.perfectdreams.loritta.morenitta.utils.extensions.writeSseEvent
import net.perfectdreams.loritta.morenitta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.morenitta.website.views.httpapidocs.mainframeTerminalLorifetchStats

class GetLorifetchStatsSSERoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/developers/docs/lorifetch-stats") {
    override val isMainClusterOnlyRoute = true

    override suspend fun onLocalizedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        i18nContext: I18nContext
    ) {
        // Makes SSE work behind nginx
        // https://stackoverflow.com/a/33414096/7271796
        call.response.header("X-Accel-Buffering", "no")
        call.response.cacheControl(CacheControl.NoCache(null))
        call.respondBytesWriter(contentType = ContentType.Text.EventStream) {
            loritta.newWebsite!!.lorifetch.statsFlow.collect { stats ->
                writeSseEvent(
                    SseEvent(
                        createHTML(prettyPrint = false)
                            .body {
                                mainframeTerminalLorifetchStats(
                                    loritta,
                                    i18nContext,
                                    stats.guildCount,
                                    stats.executedCommands,
                                    stats.uniqueUsersExecutedCommands,
                                    stats.currentSong
                                )
                            }
                    )
                )
                flush()
            }
        }
    }
}