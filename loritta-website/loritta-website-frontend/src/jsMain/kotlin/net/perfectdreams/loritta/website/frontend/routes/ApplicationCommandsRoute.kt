package net.perfectdreams.loritta.website.frontend.routes

import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.loritta.website.frontend.LorittaWebsiteFrontend
import net.perfectdreams.loritta.website.frontend.views.CommandsView
import net.perfectdreams.loritta.website.frontend.views.DokyoView

class ApplicationCommandsRoute(val showtime: LorittaWebsiteFrontend) : LocalizedRoute(RoutePath.APPLICATION_COMMANDS) {
    override fun onLocalizedRequest(): DokyoView {
        return CommandsView(showtime)
    }
}