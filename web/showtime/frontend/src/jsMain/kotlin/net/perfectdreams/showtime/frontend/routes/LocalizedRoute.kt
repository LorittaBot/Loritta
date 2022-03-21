package net.perfectdreams.showtime.frontend.routes

import net.perfectdreams.showtime.frontend.views.DokyoView

abstract class LocalizedRoute(val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
    override fun onRequest() = onLocalizedRequest()

    abstract fun onLocalizedRequest(): DokyoView?
}