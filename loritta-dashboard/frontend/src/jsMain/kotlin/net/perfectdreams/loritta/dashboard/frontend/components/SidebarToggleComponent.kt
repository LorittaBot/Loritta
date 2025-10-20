package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.bliss.BlissComponent
import web.cssom.ClassName
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLButtonElement
import web.pointer.CLICK
import web.pointer.PointerEvent

class SidebarToggleComponent : BlissComponent<HTMLButtonElement>() {
    override fun onMount() {
        this.registeredEvents += mountedElement.addEventHandler(PointerEvent.CLICK) {
            val sidebar = document.querySelector("#left-sidebar") ?: error("Could not find left sidebar!")
            if (sidebar.classList.contains(ClassName("is-open"))) {
                sidebar.classList.remove(ClassName("is-open"))
            } else {
                sidebar.classList.add(ClassName("is-open"))
            }

            if (sidebar.classList.contains(ClassName("is-closed"))) {
                sidebar.classList.remove(ClassName("is-closed"))
            } else {
                sidebar.classList.add(ClassName("is-closed"))
            }
        }
    }

    override fun onUnmount() {}
}