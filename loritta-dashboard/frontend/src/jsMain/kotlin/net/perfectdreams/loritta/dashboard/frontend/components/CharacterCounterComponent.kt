package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.luna.bliss.BlissComponent
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLDivElement
import web.html.HTMLInputElement
import web.html.HTMLTextAreaElement
import web.input.INPUT
import web.input.InputEvent

class CharacterCounterComponent : BlissComponent<HTMLDivElement>() {
    override fun onMount() {
        val elementToBeListenedTo = document.body.querySelector(mountedElement.getAttribute("character-counter-listen")!!) ?: error("Could not find element to listen to!")

        if (elementToBeListenedTo is HTMLInputElement) {
            fun updatePreview() {
                this.mountedElement.textContent = "${elementToBeListenedTo.value.length}/${elementToBeListenedTo.maxLength}"
            }

            registeredEvents += elementToBeListenedTo.addEventHandler(InputEvent.INPUT) {
                updatePreview()
            }

            updatePreview()
        } else if (elementToBeListenedTo is HTMLTextAreaElement) {
            fun updatePreview() {
                this.mountedElement.textContent = "${elementToBeListenedTo.value.length}/${elementToBeListenedTo.maxLength}"
            }

            registeredEvents += elementToBeListenedTo.addEventHandler(InputEvent.INPUT) {
                updatePreview()
            }

            updatePreview()
        } else error("You can't listen to a $elementToBeListenedTo!")
    }

    override fun onUnmount() {}
}