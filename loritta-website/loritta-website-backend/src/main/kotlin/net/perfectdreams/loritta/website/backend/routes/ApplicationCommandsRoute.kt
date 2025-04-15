package net.perfectdreams.loritta.website.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.serializable.SlashCommandInfo
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.userTheme
import net.perfectdreams.loritta.website.backend.views.ApplicationCommandsView

class ApplicationCommandsRoute(val showtime: LorittaWebsiteBackend) : LocalizedRoute(showtime, RoutePath.APPLICATION_COMMANDS) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        try {
            call.respondHtml(
                block = ApplicationCommandsView(
                    showtime,
                    call.request.userTheme,
                    locale,
                    i18nContext,
                    "/commands/slash",
                    // Only show slash commands
                    showtime.commands.applicationCommandsInfo.filterIsInstance<SlashCommandInfo>(),
                    call.parameters["category"]?.uppercase()?.let {
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