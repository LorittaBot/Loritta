package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.bliss.BlissComponent
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLDivElement
import web.html.HTMLInputElement
import web.input.INPUT
import web.input.InputEvent

class CharacterCounterComponent : BlissComponent<HTMLDivElement>() {
    override fun onMount() {
        val elementToBeListenedTo = document.body.querySelector(mountedElement.getAttribute("character-counter-listen")!!) ?: error("Could not find element to listen to!")
        require(elementToBeListenedTo is HTMLInputElement)

        fun updatePreview() {
            this.mountedElement.textContent = "${elementToBeListenedTo.value.length}/${elementToBeListenedTo.maxLength}"
        }

        registeredEvents += elementToBeListenedTo.addEventHandler(InputEvent.INPUT) {
            updatePreview()
        }

        updatePreview()
    }

    override fun onUnmount() {}
}