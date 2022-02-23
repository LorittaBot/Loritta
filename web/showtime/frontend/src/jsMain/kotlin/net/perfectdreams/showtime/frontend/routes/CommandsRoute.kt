package net.perfectdreams.showtime.frontend.routes

import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.showtime.frontend.ShowtimeFrontend
import net.perfectdreams.showtime.frontend.views.CommandsView
import net.perfectdreams.showtime.frontend.views.DokyoView

class CommandsRoute(val showtime: ShowtimeFrontend) : LocalizedRoute(RoutePath.LEGACY_COMMANDS) {
    override fun onLocalizedRequest(): DokyoView? {
        println("onLocalizedRequest()")

        return CommandsView(showtime)
    }
}