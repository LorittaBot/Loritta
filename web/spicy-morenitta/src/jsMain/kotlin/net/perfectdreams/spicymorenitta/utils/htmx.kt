package net.perfectdreams.spicymorenitta.utils

// We include htmx.js on the head, because FOR SOME GODAWFUL REASON THIS WAS NOT WORKING (EVEN THO IT WAS WORKING BEFORE) WHEN INCLUDING IT IN build.gradle.kts
// The issue COULD BE related that they changed the TS declarations to use namespace (what does that mean for us?)
external val htmx: Htmx = definedExternally

external class Htmx {
    fun trigger(elt: dynamic, name: String, detail: dynamic)

    fun process(elt: dynamic)

    fun find(selector: String): dynamic

    fun values(element: dynamic): dynamic
}