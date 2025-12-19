package net.perfectdreams.luna.bliss

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
                    // MutationObserver is "async" (it doesn't run EXACTLY when it is removed from the DOM)
                    // The moveBefore calls a remove + add, which causes wonky behavior with "bliss-preserve"
                    // To workaround this, we check if the body contains the node or not
                    // If it doesn't, then everything is good :)
                    // If it does, then the node is actually still on the DOM (probably moved with moveBefore)
                    if (document.body.contains(n))
                        return@MutationObserver

                    observer.disconnect()
                    block()
                    return@MutationObserver // Bye!
                }
            }
        }
    }

    observer.observe(document.body, MutationObserverInit(childList = true, subtree = true))
}