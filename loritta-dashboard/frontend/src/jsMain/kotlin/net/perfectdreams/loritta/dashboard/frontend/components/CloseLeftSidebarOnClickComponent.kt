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
            m.closeLeftSidebar()
        }
    }

    override fun onUnmount() {}
}