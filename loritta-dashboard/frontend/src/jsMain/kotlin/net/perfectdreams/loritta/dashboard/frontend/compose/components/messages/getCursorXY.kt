package net.perfectdreams.loritta.dashboard.frontend.compose.components.messages

import web.dom.document
import web.dom.getComputedStyle
import web.html.HTMLDivElement
import web.html.HTMLSpanElement
import web.html.HTMLTextAreaElement

/**
 * Returns x, y coordinates for absolute positioning of a span within a given text input
 * at a given selection point
 *
 * @param {object} input - the input element to obtain coordinates for
 * @param {number} selectionPoint - the selection point for the input
 */
fun getCursorXY(input: HTMLTextAreaElement, selectionPoint: Int): CursorXY {
    val taRect = input.getBoundingClientRect()
    val computedStyle = getComputedStyle(input)

    // Build a hidden mirror
    val div = document.createElement("div") as HTMLDivElement

    val rules = arrayOf(
        "boxSizing",
        "width",
        "height",
        "overflowY",
        "borderTopWidth",
        "borderRightWidth",
        "borderBottomWidth",
        "borderLeftWidth",
        "paddingTop",
        "paddingRight",
        "paddingBottom",
        "paddingLeft",
        "fontStyle",
        "fontVariant",
        "fontWeight",
        "fontStretch",
        "fontSize",
        "fontFamily",
        "lineHeight",
        "letterSpacing",
        "textTransform",
        "textAlign",
        "textIndent",
        "whiteSpace",
        "wordWrap",
        "tabSize"
    )

    for (rule in rules) {
        div.style.setProperty(rule, computedStyle.getPropertyValue(rule))
    }

    div.style.position = "absolute"
    div.style.visibility = "hidden"
    div.style.whiteSpace = "pre-wrap"
    div.style.asDynamic().wordWrap = "break-word"
    div.style.overflow = "hidden"

    div.textContent = input.value.substring(0, selectionPoint)
    val span = document.createElement("span") as HTMLSpanElement
    span.textContent = input.value.substring(selectionPoint).ifEmpty { "." }

    // Ensure the fallback char doesn't shift x visually if used
    span.style.opacity = "0"
    div.appendChild(span)

    document.body.appendChild(div)

    val divRect = div.getBoundingClientRect()
    val spanRect = span.getBoundingClientRect()

    val x = taRect.left + (spanRect.left - divRect.left) - input.scrollLeft
    val y = taRect.top + (spanRect.top - divRect.top) - input.scrollTop

    return CursorXY(
        x.toInt(),
        y.toInt()
    )
}

data class CursorXY(val x: Int, val y: Int)