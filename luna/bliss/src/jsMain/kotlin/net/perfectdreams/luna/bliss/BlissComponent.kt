package net.perfectdreams.luna.bliss

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import web.dom.Element

abstract class BlissComponent<T : Element> {
    val scope = CoroutineScope(Job())
    private var _mountedElement: T? = null
    val mountedElement: T
        get() = _mountedElement ?: error("Element is not mounted!")
    val registeredEvents = mutableListOf<() -> Unit>()

    fun mount(element: Element) {
        check(_mountedElement == null) { "Element is already mounted!" }
        _mountedElement = element as T

        onMount()
    }

    fun unmount() {
        this.scope.cancel()
        for (unregisterCall in registeredEvents) {
            unregisterCall()
        }
        this.registeredEvents.clear()
        onUnmount()
        this._mountedElement = null
    }

    abstract fun onMount()

    abstract fun onUnmount()

    open fun onElementSwap(element: Element) {}
}