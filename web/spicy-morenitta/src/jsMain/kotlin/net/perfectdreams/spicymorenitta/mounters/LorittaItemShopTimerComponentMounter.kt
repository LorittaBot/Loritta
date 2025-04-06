package net.perfectdreams.spicymorenitta.mounters

import kotlinx.coroutines.*
import net.perfectdreams.i18nhelper.formatters.IntlMessageFormat
import net.perfectdreams.spicymorenitta.utils.Logging
import net.perfectdreams.spicymorenitta.utils.htmx
import net.perfectdreams.spicymorenitta.utils.jsObject
import web.dom.document
import web.html.HTMLElement
import web.mutation.MutationObserver
import web.mutation.MutationObserverInit
import kotlin.js.Date

class LorittaItemShopTimerComponentMounter : SimpleComponentMounter("loritta-item-shop-timer"), Logging {
    override fun simpleMount(element: HTMLElement) {
        if (element.getAttribute("loritta-powered-up") != null)
            return

        element.setAttribute("loritta-powered-up", "")
        val i18nHours = element.getAttribute("loritta-item-shop-i18n-hours")!!
        val i18nMinutes = element.getAttribute("loritta-item-shop-i18n-minutes")!!
        val i18nSeconds = element.getAttribute("loritta-item-shop-i18n-seconds")!!
        val messageFormatHours = IntlMessageFormat(i18nHours, "pt")
        val messageFormatMinutes = IntlMessageFormat(i18nMinutes, "pt")
        val messageFormatSeconds = IntlMessageFormat(i18nSeconds, "pt")

        val scope = CoroutineScope(Job())
        val observer = MutationObserver { _, observer ->
            if (!document.contains(element)) {
                debug("Cancelling element's coroutine scope because it was removed from the DOM...")
                console.log(element)
                scope.cancel() // Cancel coroutine scope when element is removed
                observer.disconnect() // Disconnect the observer to avoid leaks
            }
        }
        observer.observe(
            document.body,
            MutationObserverInit(childList = true, subtree = true)
        )

        val resetsAt = element.getAttribute("loritta-item-shop-resets-at")!!.toLong()

        // TODO - htmx-adventures: Don't use GlobalScope!
        //  (technically we are scoping this to the element nowadays...)
        var previousText = ""
        scope.launch {
            while (isActive) {
                val diff = resetsAt - (Date().getTime().toLong())
                if (0 >= diff) {
                    // Trigger Item Shop refresh if the time is 0
                    htmx.trigger("body", "refreshItemShop", null)
                    return@launch
                }

                val timeInSeconds = diff / 1_000

                val s = timeInSeconds % 60
                val m = (timeInSeconds / 60) % 60
                val h = (timeInSeconds / (60 * 60)) % 24

                val newText = buildString {
                    if (h != 0L) {
                        append(
                            messageFormatHours.format(
                                jsObject {
                                    this.unit = h + 1
                                }
                            )
                        )
                    } else if (m != 0L) {
                        append(
                            messageFormatMinutes.format(
                                jsObject {
                                    this.unit = m
                                }
                            )
                        )
                    } else if (s != 0L) {
                        append(
                            messageFormatSeconds.format(
                                jsObject {
                                    this.unit = s
                                }
                            )
                        )
                    }
                }
                if (newText != previousText) {
                    debug("Updating timer text to $newText")
                    element.innerText = newText
                    previousText = newText
                }

                delay(1_000)
            }
        }
    }
}