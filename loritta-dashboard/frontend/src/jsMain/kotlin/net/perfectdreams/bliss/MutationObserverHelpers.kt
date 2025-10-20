package net.perfectdreams.bliss

import js.array.asList
import web.dom.Element
import web.dom.Node
import web.dom.document
import web.mutation.MutationObserver
import web.mutation.MutationObserverInit

/**
 * Invokes [block] when the element [this] is removed from the DOM
 */
fun Element.whenRemovedFromDOM(block: () -> (Unit)) {
    val observer = MutationObserver { mutationList, observer ->
        for (record in mutationList) {
            for (n in record.removedNodes.asList()) {
                if (n == this || (n.nodeType == Node.ELEMENT_NODE && n.contains(this))) {
                    observer.disconnect()
                    block()
                    return@MutationObserver // Bye!
                }
            }
        }
    }

    observer.observe(document.body, MutationObserverInit(childList = true, subtree = true))
}