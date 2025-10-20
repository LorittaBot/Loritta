package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.bliss.BlissComponent
import web.cssom.ClassName
import web.dom.Element
import web.events.CHANGE
import web.events.Event
import web.events.addEventHandler
import web.html.HTMLDivElement
import web.html.HTMLInputElement

class ToggleableSectionComponent : BlissComponent<HTMLDivElement>() {
    override fun onMount() {
        val toggle = mountedElement.querySelector(".toggle-wrapper input[type=\"checkbox\"]") as HTMLInputElement

        this.registeredEvents += toggle.addEventHandler(Event.CHANGE) {
            if (toggle.checked) {
                mountedElement.classList.add(ClassName("is-open"))
            } else {
                mountedElement.classList.remove(ClassName("is-open"))
            }
        }
    }

    override fun onUnmount() {}
}