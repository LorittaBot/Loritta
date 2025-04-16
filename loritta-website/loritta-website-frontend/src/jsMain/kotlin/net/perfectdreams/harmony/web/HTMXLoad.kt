package net.perfectdreams.harmony.web

import web.events.Event
import web.events.EventType
import web.html.HTMLElement

open external class HTMXLoad(
    override val type: EventType<HTMXLoad>
) : Event {
    val detail: LoadDetails
}

open external class LoadDetails {
    val elt: HTMLElement
}