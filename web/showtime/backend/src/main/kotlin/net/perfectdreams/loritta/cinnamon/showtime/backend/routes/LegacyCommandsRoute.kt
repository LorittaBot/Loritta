package net.perfectdreams.loritta.cinnamon.showtime.backend.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.showtime.backend.ShowtimeBackend
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.userTheme
import net.perfectdreams.loritta.cinnamon.showtime.backend.views.LegacyCommandsView
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.serializable.CommandInfo

class LegacyCommandsRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.LEGACY_COMMANDS) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        try {
            call.respondHtml(
                block = LegacyCommandsView(
                    showtime,
                    call.request.userTheme,
                    locale,
                    i18nContext,
                    "/commands/legacy",
                    showtime.commands.legacyCommandsInfo,
                    call.parameters["category"]?.uppercase()?.let {
                        try {
                            CommandCategory.valueOf(it)
                        } catch (e: Throwable) {
                            null
                        }
                    },
                    loritta.legacyLorittaCommands.additionalCommandsInfo
                ).generateHtml()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}