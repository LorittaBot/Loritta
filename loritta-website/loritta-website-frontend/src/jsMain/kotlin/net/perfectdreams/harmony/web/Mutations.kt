package net.perfectdreams.harmony.web

import web.dom.Element
import web.dom.Node

/** Removes all the children from this node. */
@SinceKotlin("1.4")
public fun Node.clear() {
    while (hasChildNodes()) {
        removeChild(firstChild!!)
    }
}

/**
 * Creates text node and append it to the element.
 *
 * @return this element
 */
@SinceKotlin("1.4")
public fun Element.appendText(text: String): Element {
    appendChild(ownerDocument.createTextNode(text))
    return this
}
