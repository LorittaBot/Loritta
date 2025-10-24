package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.bliss.BlissComponent
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import web.cssom.ClassName
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLDivElement
import web.html.HTMLElement
import web.pointer.CLICK
import web.pointer.PointerEvent
import web.window.window

class CloseLeftSidebarOnClickComponent(val m: LorittaDashboardFrontend) : BlissComponent<HTMLElement>() {
    override fun onMount() {
        this.registeredEvents += this.mountedElement.addEventHandler(PointerEvent.CLICK) {
            val sidebar = document.querySelector("#left-sidebar") ?: error("Could not find left sidebar!")
            if (sidebar.classList.contains(ClassName("is-open"))) {
                sidebar.classList.remove(ClassName("is-open"))
                sidebar.classList.add(ClassName("is-closed"))
            }
        }
    }

    override fun onUnmount() {}
}