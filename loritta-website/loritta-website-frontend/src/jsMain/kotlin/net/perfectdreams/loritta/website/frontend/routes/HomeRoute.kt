package net.perfectdreams.loritta.website.frontend.routes

import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.loritta.website.frontend.LorittaWebsiteFrontend
import net.perfectdreams.loritta.website.frontend.views.DokyoView
import net.perfectdreams.loritta.website.frontend.views.HomeView

class HomeRoute(val showtime: LorittaWebsiteFrontend) : LocalizedRoute(RoutePath.HOME) {
    override fun onLocalizedRequest(): DokyoView? {
        println("onLocalizedRequest()")

        return HomeView(showtime)
    }
}