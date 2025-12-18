package net.perfectdreams.luna.bliss

import web.dom.Element

/**
 * Gets the [BlissComponent] of the [this]
 *
 * If the element does not have any [BlissComponent], a empty list is returned
 */
var Element.blissComponents: List<BlissComponent<*>>
    get() {
        if (this.asDynamic().blissComponents == undefined)
            return emptyList()

        return this.asDynamic().blissComponents as List<BlissComponent<*>>
    }
    set(value) {
        if (value.isEmpty())
            this.asDynamic().blissComponents = undefined
        else
            this.asDynamic().blissComponents = value
    }

/**
 * Gets the first [BlissComponent] of [this] that matches [T]
 */
inline fun <reified T : BlissComponent<*>> Element.getBlissComponent(): T {
    return this.blissComponents.filterIsInstance<T>().first()
}