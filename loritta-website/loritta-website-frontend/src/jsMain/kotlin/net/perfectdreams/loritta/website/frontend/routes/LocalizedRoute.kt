package net.perfectdreams.loritta.website.frontend.routes

import net.perfectdreams.loritta.website.frontend.views.DokyoView

abstract class LocalizedRoute(val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
    override fun onRequest() = onLocalizedRequest()

    abstract fun onLocalizedRequest(): DokyoView?
}