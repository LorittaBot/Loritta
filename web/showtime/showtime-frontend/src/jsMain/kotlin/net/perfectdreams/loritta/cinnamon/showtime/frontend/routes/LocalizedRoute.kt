package net.perfectdreams.loritta.cinnamon.showtime.frontend.routes

import net.perfectdreams.loritta.cinnamon.showtime.frontend.views.DokyoView

abstract class LocalizedRoute(val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
    override fun onRequest() = onLocalizedRequest()

    abstract fun onLocalizedRequest(): DokyoView?
}