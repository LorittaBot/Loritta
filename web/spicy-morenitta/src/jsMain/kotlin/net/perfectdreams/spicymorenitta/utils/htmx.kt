@file:JsModule("htmx.org")
@file:JsNonModule
package net.perfectdreams.spicymorenitta.utils

external fun trigger(elt: dynamic, name: String, detail: dynamic)

external fun process(elt: dynamic)

external fun find(selector: String): dynamic

external fun values(element: dynamic): dynamic