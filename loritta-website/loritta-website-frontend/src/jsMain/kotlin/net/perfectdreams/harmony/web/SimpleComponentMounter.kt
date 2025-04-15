package net.perfectdreams.harmony.web

import web.html.HTMLElement

/**
 * A simple component mounter
 */
abstract class SimpleComponentMounter(val id: String) {
    abstract fun mount(element: HTMLElement)
}