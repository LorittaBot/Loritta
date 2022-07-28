package net.perfectdreams.loritta.cinnamon.showtime.frontend.routes

import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.loritta.cinnamon.showtime.frontend.ShowtimeFrontend
import net.perfectdreams.loritta.cinnamon.showtime.frontend.views.CommandsView
import net.perfectdreams.loritta.cinnamon.showtime.frontend.views.DokyoView

class ApplicationCommandsRoute(val showtime: ShowtimeFrontend) : LocalizedRoute(RoutePath.APPLICATION_COMMANDS) {
    override fun onLocalizedRequest(): DokyoView {
        return CommandsView(showtime)
    }
}