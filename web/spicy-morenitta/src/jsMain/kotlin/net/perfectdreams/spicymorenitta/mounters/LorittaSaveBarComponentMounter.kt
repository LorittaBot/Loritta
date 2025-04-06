package net.perfectdreams.spicymorenitta.mounters

import net.perfectdreams.spicymorenitta.utils.Logging
import net.perfectdreams.spicymorenitta.utils.addClass
import net.perfectdreams.spicymorenitta.utils.querySelector
import net.perfectdreams.spicymorenitta.utils.removeClass
import web.dom.document
import web.html.HTMLDivElement
import web.html.HTMLElement
import web.mutation.MutationObserver
import web.mutation.MutationObserverInit

class LorittaSaveBarComponentMounter : SimpleComponentMounter("loritta-save-bar"), Logging {
    override fun simpleMount(element: HTMLElement) {
        if (element.getAttribute("loritta-powered-up") != null)
            return

        element.setAttribute("loritta-powered-up", "")

        debug("Loritta Save Bar")
        val observer = MutationObserver { _, observer ->
            debug("DOM mutation")
            if (!document.contains(element)) {
                debug("Cancelling element's save bar scope because it was removed from the DOM...")
                document.querySelector<HTMLDivElement?>(".toast-list")?.removeClass("save-bar-active")
                observer.disconnect() // Disconnect the observer to avoid leaks
            } else {
                if (element.classList.contains("has-changes")) {
                    debug("I have changes!")
                    document.querySelector<HTMLDivElement?>(".toast-list")?.addClass("save-bar-active")
                } else if (element.classList.contains("no-changes")) {
                    debug("I don't have changes...")
                    document.querySelector<HTMLDivElement?>(".toast-list")?.removeClass("save-bar-active")
                }
            }
        }
        observer.observe(
            document.body,
            MutationObserverInit(childList = true, subtree = true, attributes = true)
        )
    }
}