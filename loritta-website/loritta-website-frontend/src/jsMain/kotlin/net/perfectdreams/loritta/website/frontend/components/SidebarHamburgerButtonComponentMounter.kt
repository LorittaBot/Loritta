package net.perfectdreams.loritta.website.frontend.components

import net.perfectdreams.harmony.web.SimpleComponentMounter
import net.perfectdreams.harmony.web.addClass
import net.perfectdreams.harmony.web.hasClass
import net.perfectdreams.harmony.web.removeClass
import net.perfectdreams.loritta.website.frontend.utils.extensions.onClick
import net.perfectdreams.loritta.website.frontend.utils.extensions.select
import web.dom.document
import web.html.HTMLDivElement
import web.html.HTMLElement

class SidebarHamburgerButtonComponentMounter : SimpleComponentMounter("sidebar-opener") {
    override fun mount(element: HTMLElement) {
        element.onClick {
            val sidebar = document.select<HTMLDivElement>("#left-sidebar")

            if (sidebar.hasClass("is-open")) {
                sidebar.addClass("is-closed")
                sidebar.removeClass("is-open")
            } else {
                sidebar.addClass("is-open")
                sidebar.removeClass("is-closed")
            }
        }
    }
}