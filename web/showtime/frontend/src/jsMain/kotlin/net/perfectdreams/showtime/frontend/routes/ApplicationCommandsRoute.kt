package net.perfectdreams.showtime.frontend.routes

import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.showtime.frontend.ShowtimeFrontend
import net.perfectdreams.showtime.frontend.views.CommandsView
import net.perfectdreams.showtime.frontend.views.DokyoView

class ApplicationCommandsRoute(val showtime: ShowtimeFrontend) : LocalizedRoute(RoutePath.APPLICATION_COMMANDS) {
    override fun onLocalizedRequest(): DokyoView {
        return CommandsView(showtime)
    }
}