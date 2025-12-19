package net.perfectdreams.dora.frontend

import js.array.asList
import kotlinx.serialization.json.Json
import net.perfectdreams.luna.bliss.Bliss
import net.perfectdreams.luna.bliss.BlissHex
import net.perfectdreams.luna.modals.EmbeddedModal
import net.perfectdreams.luna.modals.ModalManager
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.ToastManager
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLElement
import web.pointer.CLICK
import web.pointer.PointerEvent

class DoraFrontend {
    val toastManager = ToastManager({}) {}
    val modalManager = ModalManager {
        Bliss.processAttributes(it)
        onDispose {}
    }

    fun start() {
        Bliss.setupEvents()

        Bliss.registerElementProcessor { element ->
            val modalOnClick = element.getAttribute("bliss-open-modal-on-click")

            if (modalOnClick != null) {
                val content = element.getAttribute("bliss-modal") ?: error("Missing bliss-modal attribute on a bliss-open-modal-on-click!")
                val modal = Json.decodeFromString<EmbeddedModal>(BlissHex.decodeFromHexString(content))

                element.addEventHandler(PointerEvent.CLICK) {
                    this.modalManager.openModal(modal)
                }
            }
        }

        Bliss.registerElementProcessor { element ->
            val closeModalOnClick = element.getAttribute("bliss-close-modal-on-click")

            if (closeModalOnClick != null) {
                element.addEventHandler(PointerEvent.CLICK) {
                    this.modalManager.closeModal()
                }
            }
        }

        Bliss.processAttributes(document.body)

        toastManager.render(document.querySelector("#toast-list") as HTMLElement)
        modalManager.render(document.querySelector("#modal-list") as HTMLElement)

        Bliss.registerDocumentParsedEventListener { parsedDocument ->
            // The "close modals" should ALWAYS be processed first, this way we can do something like this:
            // 1. Request to close all modals
            // 2. Request to open a new modal
            val closeModalHack = parsedDocument.querySelectorAll("[bliss-close-modal]")
            for (element in closeModalHack.asList()) {
                element.remove()
                this.modalManager.closeModal()
            }

            val closeAllModalsHack = parsedDocument.querySelectorAll("[bliss-close-all-modals]")
            for (element in closeAllModalsHack.asList()) {
                element.remove()
                this.modalManager.closeAllModals()
            }

            val showToastHack = parsedDocument.querySelectorAll("[bliss-show-toast]")
            for (element in showToastHack.asList()) {
                val content = element.getAttribute("bliss-toast") ?: error("Missing bliss-toast attribute on a bliss-show-toast!")
                val toast = Json.decodeFromString<EmbeddedToast>(BlissHex.decodeFromHexString(content))
                element.remove()

                this.toastManager.showToast(toast)
            }

            val showModalHack = parsedDocument.querySelectorAll("[bliss-show-modal]")
            for (element in showModalHack.asList()) {
                val content = element.getAttribute("bliss-modal") ?: error("Missing bliss-toast attribute on a bliss-modal!")
                val modal = Json.decodeFromString<EmbeddedModal>(BlissHex.decodeFromHexString(content))
                element.remove()

                this.modalManager.openModal(modal)
            }
        }
    }
}