package net.perfectdreams.harmony.web

import web.events.EventType

object HTMXEvents {
    val HTMX_BEFORE_REQUEST = EventType<HTMXBeforeRequest>("htmx:beforeRequest")

    val HTMX_LOAD = EventType<HTMXLoad>("htmx:load")
}