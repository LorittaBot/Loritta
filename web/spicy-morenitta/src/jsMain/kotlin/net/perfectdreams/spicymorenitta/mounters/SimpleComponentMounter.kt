package net.perfectdreams.spicymorenitta.mounters

import net.perfectdreams.spicymorenitta.utils.Logging
import web.html.HTMLElement

abstract class SimpleComponentMounter(val componentId: String) : ComponentMounter(), Logging {
    override fun mount(element: HTMLElement) {
        val componentMounterId = element.getAttribute("data-component-mounter") ?: return

        if (componentMounterId == this.componentId) {
            info("Attempting to *simple* mount $componentMounterId at $element")
            simpleMount(element)
        } else {
            warn("Unknown component mounter ID $componentMounterId! Bug?")
        }
    }

    abstract fun simpleMount(element: HTMLElement)
}