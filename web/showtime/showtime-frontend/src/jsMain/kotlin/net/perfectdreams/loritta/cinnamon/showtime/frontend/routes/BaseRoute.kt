package net.perfectdreams.loritta.cinnamon.showtime.frontend.routes

import net.perfectdreams.loritta.cinnamon.showtime.frontend.views.DokyoView

abstract class BaseRoute(val path: String) {
    abstract fun onRequest(): DokyoView?
}