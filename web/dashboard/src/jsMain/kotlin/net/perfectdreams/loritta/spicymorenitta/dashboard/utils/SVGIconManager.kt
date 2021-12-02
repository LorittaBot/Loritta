package net.perfectdreams.loritta.spicymorenitta.dashboard.utils

import org.w3c.dom.Element
import org.w3c.dom.asList
import org.w3c.dom.parsing.DOMParser

// Inspired by Loritta's Showtime "SVGIconManager" class
object SVGIconManager {
    val registeredSvgs = mutableMapOf<String, SVGIcon>()

    val tailSpin by lazy { register("tail-spin", svgTailSpin) }
    val sparkles by lazy { register("sparkles", svgSparkles, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val bars by lazy { register("bars", svgBars, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val times by lazy { register("times", svgTimes, SVGOptions.ADD_CURRENT_COLOR_FILLS) }

    /**
     * Loads and registers a SVG with [name] and [path]
     *
     * The SVG also checks for name conflicts and stores all registered icons in [registeredSvgs]
     */
    fun register(name: String, html: String, vararg options: SVGOptions): SVGIcon {
        if (name in registeredSvgs)
            throw RuntimeException("There is already a SVG with name $name!")

        val parser = DOMParser()
        val document = parser.parseFromString(html, "image/svg+xml")

        val svgTag = document.getElementsByTagName("svg")
            .asList()
            .first()

        // TODO: Check if we really need this
        // svgTag.addClass("icon") // Add the "icon" class name to the SVG root, this helps us styling it via CSS
        //    .addClass("icon-$name") // Also add the icon name to the SVG root, so we can individually style with CSS

        if (SVGOptions.REMOVE_FILLS in options) {
            // Remove all "fill" tags
            svgTag.querySelectorAll("[fill]")
                .asList()
                .filterIsInstance<Element>()
                .forEach {
                    it.removeAttribute("fill")
                }
        }

        if (SVGOptions.ADD_CURRENT_COLOR_FILLS in options) {
            // Adds "currentColor" fills
            svgTag.querySelectorAll("path")
                .asList()
                .filterIsInstance<Element>()
                .forEach {
                    it.setAttribute("fill", "currentColor")
                }
        }

        val svgIcon = SVGIcon(svgTag)
        registeredSvgs[name] = svgIcon
        return svgIcon
    }

    class SVGIcon(val element: Element)

    enum class SVGOptions {
        REMOVE_FILLS,
        ADD_CURRENT_COLOR_FILLS,
    }
}

// Needs to be top level!
@JsModule("./icons/tail-spin.svg")
@JsNonModule
external val svgTailSpin: dynamic

@JsModule("./icons/twemoji/2728.svg")
@JsNonModule
external val svgSparkles: dynamic

@JsModule("./icons/fontawesome5/bars.svg")
@JsNonModule
external val svgBars: dynamic

@JsModule("./icons/fontawesome5/times.svg")
@JsNonModule
external val svgTimes: dynamic