package net.perfectdreams.showtime.frontend.routes

import net.perfectdreams.showtime.frontend.views.DokyoView

abstract class BaseRoute(val path: String) {
    abstract fun onRequest(): DokyoView?
}