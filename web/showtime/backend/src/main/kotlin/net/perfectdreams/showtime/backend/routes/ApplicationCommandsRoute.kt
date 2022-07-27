package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.server.html.*
import io.ktor.server.application.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.showtime.backend.PublicApplicationCommands
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.userTheme
import net.perfectdreams.showtime.backend.views.ApplicationCommandsView

class ApplicationCommandsRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.APPLICATION_COMMANDS) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        try {
            call.respondHtml(
                block = ApplicationCommandsView(
                    showtime,
                    call.request.userTheme,
                    locale,
                    i18nContext,
                    "/commands/slash",
                    showtime.publicApplicationCommands.flattenedDataDeclarations,
                    call.parameters["category"]?.toUpperCase()?.let {
                        try {
                            CommandCategory.valueOf(it)
                        } catch (e: Throwable) {
                            null
                        }
                    }
                ).generateHtml()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}