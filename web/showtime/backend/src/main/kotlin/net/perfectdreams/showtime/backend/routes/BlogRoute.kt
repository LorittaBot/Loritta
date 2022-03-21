package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.*
import io.ktor.html.*
import kotlinx.html.body
import kotlinx.html.p
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.ShowtimeBackend

class BlogRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.BLOG) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        call.respondHtml {
            body {
                showtime.loadSourceContentsFromFolder("blog").forEach {
                    p {
                        + it.localizedContents.values.first().metadata.title
                    }
                }
            }
        }
    }
}