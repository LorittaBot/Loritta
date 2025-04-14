package net.perfectdreams.loritta.website.frontend.routes

import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.loritta.website.frontend.LorittaWebsiteFrontend
import net.perfectdreams.loritta.website.frontend.views.CommandsView
import net.perfectdreams.loritta.website.frontend.views.DokyoView

class LegacyCommandsRoute(val showtime: LorittaWebsiteFrontend) : LocalizedRoute(RoutePath.LEGACY_COMMANDS) {
    override fun onLocalizedRequest(): DokyoView {
        return CommandsView(showtime)
    }
}