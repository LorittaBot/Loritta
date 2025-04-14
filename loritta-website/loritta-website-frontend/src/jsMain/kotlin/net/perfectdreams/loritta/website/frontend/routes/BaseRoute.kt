package net.perfectdreams.loritta.website.frontend.routes

import net.perfectdreams.loritta.website.frontend.views.DokyoView

abstract class BaseRoute(val path: String) {
    abstract fun onRequest(): DokyoView?
}