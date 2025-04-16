package net.perfectdreams.harmony.web

import web.events.Event
import web.events.EventType
import web.html.HTMLElement
import web.xhr.XMLHttpRequest

open external class HTMXBeforeRequest(
    override val type: EventType<HTMXBeforeRequest>
) : Event {
    val detail: BeforeRequestDetails
}

open external class BeforeRequestDetails {
    val elt: HTMLElement
    val xhr: XMLHttpRequest
    val target: HTMLElement
    val requestConfig: dynamic
}