package net.perfectdreams.loritta.dashboard.frontend.components

import net.perfectdreams.bliss.BlissComponent
import web.cssom.ClassName
import web.dom.Element
import web.events.CHANGE
import web.events.Event
import web.events.addEventHandler
import web.html.HTMLImageElement
import web.html.HTMLInputElement
import web.intersection.IntersectionObserver
import web.intersection.IntersectionObserverInit

/**
 * A component that changes images every time the image is visible on the viewport
 *
 * In reality, we switch while the element is *not* visible, to avoid "flickers"
 */
class RotatingImageComponent : BlissComponent<HTMLImageElement>() {
    var observer: IntersectionObserver? = null

    override fun onMount() {
        val imageUrls = mountedElement.getAttribute("rotating-image-urls")!!.split(",")

        if (imageUrls.isEmpty())
            error("Missing URLs on a rotating-image-urls!")

        if (imageUrls.size == 1) {
            // There's only one image, so let's set the only URL and bail out!
            mountedElement.setAttribute("src", imageUrls.first())
            return
        }

        // Set a random image to start with if the src was not set before
        if (mountedElement.getAttribute("src").isNullOrBlank())
            mountedElement.setAttribute("src", imageUrls.random())

        val observer = IntersectionObserver(
            { entries, _ ->
                for (entry in entries) {
                    // We will actually change AFTER it isn't visible anymore
                    // This way we avoid the user noticing the image changing
                    if (!entry.isIntersecting) {
                        while (true) {
                            // Select a new URL that isn't the previous already existing one
                            val currentUrl = mountedElement.getAttribute("src")
                            val newUrl = imageUrls.random()

                            if (currentUrl != newUrl) {
                                mountedElement.setAttribute("src", newUrl)
                                break
                            }
                        }
                    }
                }
            },
            IntersectionObserverInit(
                root = null,
                rootMargin = "0px 0px 0px 0px"
            )
        )

        this.observer = observer
        observer.observe(mountedElement)
    }

    override fun onUnmount() {
        this.observer?.disconnect()
        this.observer = null
    }
}