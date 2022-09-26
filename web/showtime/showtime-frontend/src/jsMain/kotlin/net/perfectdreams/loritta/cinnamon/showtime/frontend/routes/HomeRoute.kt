package net.perfectdreams.loritta.cinnamon.showtime.frontend.routes

import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.loritta.cinnamon.showtime.frontend.ShowtimeFrontend
import net.perfectdreams.loritta.cinnamon.showtime.frontend.views.DokyoView
import net.perfectdreams.loritta.cinnamon.showtime.frontend.views.HomeView

class HomeRoute(val showtime: ShowtimeFrontend) : LocalizedRoute(RoutePath.HOME) {
    override fun onLocalizedRequest(): DokyoView? {
        println("onLocalizedRequest()")

        return HomeView(showtime)
    }
}