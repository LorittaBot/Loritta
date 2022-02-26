package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.*
import io.ktor.html.*
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
                    call.request.userTheme,
                    showtime.svgIconManager,
                    showtime.hashManager,
                    locale,
                    "/commands/slash",
                    i18nContext,
                    PublicApplicationCommands.flattenedDataDeclarations,
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