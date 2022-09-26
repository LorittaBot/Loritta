package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils


import org.jetbrains.compose.web.attributes.AutoComplete.Companion.name
import org.w3c.dom.Element
import org.w3c.dom.asList
import org.w3c.dom.parsing.DOMParser

// Inspired by Loritta's Showtime "SVGIconManager" class
object SVGIconManager {
    val sparkles by lazy { register(svgSparkles, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val heart by lazy { register(svgHeart, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val exclamationTriangle by lazy { register(svgExclamationTriangle, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val bars by lazy { register(svgBars, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val times by lazy { register(svgTimes, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val cogs by lazy { register(svgCogs, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val idCard by lazy { register(svgIdCard, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val images by lazy { register(svgImages, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val moneyBillWave by lazy { register(svgMoneyBillWave, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val store by lazy { register(svgStore, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val shoppingCart by lazy { register(svgShoppingCart, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val asterisk by lazy { register(svgAsterisk, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val star by lazy { register(svgStar, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val chevronDown by lazy { register(svgChevronDown, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val clock by lazy { register(svgClock, SVGOptions.ADD_CURRENT_COLOR_FILLS) }

    /**
     * Loads and registers a SVG with [name] and [path]
     */
    fun register(html: String, vararg options: SVGOptions): SVGIcon {
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
        return svgIcon
    }

    class SVGIcon(val element: Element)

    enum class SVGOptions {
        REMOVE_FILLS,
        ADD_CURRENT_COLOR_FILLS,
    }
}

// ===[ NEEDS TO BE TOP LEVEL! ]===
@JsModule("./icons/twemoji-modified/sparkles.svg")
@JsNonModule
external val svgSparkles: dynamic

@JsModule("./icons/fontawesome5/solid/heart.svg")
@JsNonModule
external val svgHeart: dynamic

@JsModule("./icons/fontawesome5/solid/exclamation-triangle.svg")
@JsNonModule
external val svgExclamationTriangle: dynamic

@JsModule("./icons/fontawesome5/solid/times.svg")
@JsNonModule
external val svgTimes: dynamic

@JsModule("./icons/fontawesome5/solid/bars.svg")
@JsNonModule
external val svgBars: dynamic

@JsModule("./icons/fontawesome5/solid/cogs.svg")
@JsNonModule
external val svgCogs: dynamic

@JsModule("./icons/fontawesome5/solid/id-card.svg")
@JsNonModule
external val svgIdCard: dynamic

@JsModule("./icons/fontawesome5/solid/images.svg")
@JsNonModule
external val svgImages: dynamic

@JsModule("./icons/fontawesome5/solid/money-bill-wave.svg")
@JsNonModule
external val svgMoneyBillWave: dynamic

@JsModule("./icons/fontawesome5/solid/store.svg")
@JsNonModule
external val svgStore: dynamic

@JsModule("./icons/fontawesome5/solid/shopping-cart.svg")
@JsNonModule
external val svgShoppingCart: dynamic

@JsModule("./icons/fontawesome5/solid/asterisk.svg")
@JsNonModule
external val svgAsterisk: dynamic

@JsModule("./icons/fontawesome5/solid/star.svg")
@JsNonModule
external val svgStar: dynamic

@JsModule("./icons/fontawesome5/solid/chevron-down.svg")
@JsNonModule
external val svgChevronDown: dynamic

@JsModule("./icons/fontawesome5/solid/clock.svg")
@JsNonModule
external val svgClock: dynamic