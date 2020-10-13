package net.perfectdreams.spicymorenitta.routes

import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLDivElement
import kotlinx.browser.document

open class UpdateNavbarSizePostRender(path: String, override val requiresUserIdentification: Boolean = true, override val requiresLocales: Boolean= true) : BaseRoute(path) {
    override fun onRender(call: ApplicationCall) {
        super.onRender(call)
        fixDummyNavbarHeight(call)
    }

    open fun fixDummyNavbarHeight(call: ApplicationCall) {
        // Atualizar o tamanho da navbar dependendo do tamanho da navbar atualmente
        val navigationBar = document.select<HTMLDivElement?>("#navigation-bar")
        if (navigationBar != null) {
            val navbarHeight = document.select<HTMLDivElement>("#navigation-bar").clientHeight.toString()

            debug("Navbar height is $navbarHeight, fixing dummy navbar")

            document.select<HTMLDivElement>("#dummy-navbar").style.height = "${navbarHeight}px"
            document.select<HTMLDivElement?>("#sidebar-wrapper")?.style?.height = "calc(100% - ${navbarHeight}px);" // Arrumar o tamanho do wrapper, caso seja necessário
        } else {
            warn("Uhhhh, navigation bar doesn't exist so... We don't need to fix it. Yay?")
        }
    }
}