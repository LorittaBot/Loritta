package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.bliss.BlissComponent
import web.cssom.ClassName
import web.cssom.MediaQuery
import web.cssom.matchMedia
import web.dom.Element
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLButtonElement
import web.pointer.*
import kotlin.math.abs

class SidebarToggleComponent : BlissComponent<HTMLButtonElement>() {
    companion object {
        private const val SWIPE_THRESHOLD = 50
        private const val IGNORE_IF_Y_THRESHOLD = 30
    }

    override fun onMount() {
        this.registeredEvents += mountedElement.addEventHandler(PointerEvent.CLICK) {
            val sidebar = document.querySelector("#left-sidebar") ?: error("Could not find left sidebar!")

            if (sidebar.classList.contains(ClassName("is-open"))) {
                sidebar.classList.add(ClassName("is-closed"))
                sidebar.classList.remove(ClassName("is-open"))
            } else {
                sidebar.classList.add(ClassName("is-open"))
                sidebar.classList.remove(ClassName("is-closed"))
            }
        }

        // Only register open/close sidebar swipes if the device is coarse (touch)
        if (matchMedia(MediaQuery("(pointer: coarse)")).matches) {
            registerOpenSidebarWithSwipe()
            registerCloseSidebarWithSwipe()
        }
    }

    fun registerOpenSidebarWithSwipe() {
        val rightSidebar = document.querySelector("#right-sidebar") ?: error("Could not find right sidebar!")

        onHorizontalSwipe(
            rightSidebar,
            onLeftToRightSwipe = {
                val sidebar = document.querySelector("#left-sidebar") ?: error("Could not find left sidebar!")

                sidebar.classList.remove(ClassName("is-closed"))
                sidebar.classList.add(ClassName("is-open"))
            }
        )
    }

    fun registerCloseSidebarWithSwipe() {
        val leftSidebar = document.querySelector("#left-sidebar") ?: error("Could not find right sidebar!")

        onHorizontalSwipe(
            leftSidebar,
            onRightToLeftSwipe = {
                leftSidebar.classList.add(ClassName("is-closed"))
                leftSidebar.classList.remove(ClassName("is-open"))
            }
        )
    }

    fun onHorizontalSwipe(
        targetElement: Element,
        onLeftToRightSwipe: () -> (Unit) = {},
        onRightToLeftSwipe: () -> (Unit) = {}
    ) {
        var startX = 0.0
        var startY = 0.0
        var tracking = false

        this.registeredEvents += targetElement.addEventHandler(PointerEvent.POINTER_DOWN) {
            tracking = true

            startX = it.x
            startY = it.y
        }

        this.registeredEvents += targetElement.addEventHandler(PointerEvent.POINTER_MOVE) {
            if (tracking) {
                val dx = it.x - startX
                val dy = it.y - startY

                if (abs(dy) >= IGNORE_IF_Y_THRESHOLD) {
                    // Stop tracking if the Y threshold is too large
                    tracking = false
                    return@addEventHandler
                }

                // To left to right means that the dx would be BIGGER than the threshold
                if (dx >= SWIPE_THRESHOLD) {
                    onLeftToRightSwipe()
                    tracking = false
                    return@addEventHandler
                }

                // To right to left means that the dx would be SMALLER than the threshold (negative)
                if (-SWIPE_THRESHOLD >= dx) {
                    onRightToLeftSwipe()
                    tracking = false
                    return@addEventHandler
                }
            }
        }

        this.registeredEvents += targetElement.addEventHandler(PointerEvent.POINTER_UP) {
            tracking = false
        }

        this.registeredEvents += targetElement.addEventHandler(PointerEvent.POINTER_CANCEL) {
            tracking = false
        }

        this.registeredEvents += targetElement.addEventHandler(PointerEvent.POINTER_LEAVE) {
            tracking = false
        }
    }

    override fun onUnmount() {}
}