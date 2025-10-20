package net.perfectdreams.loritta.dashboard.frontend.compose.components.messages

import js.array.asList
import web.dom.ElementId
import web.dom.document
import web.dom.getComputedStyle
import web.html.HTMLElement
import web.html.HTMLInputElement
import web.html.HTMLTextAreaElement

/**
 * returns x, y coordinates for absolute positioning of a span within a given text input
 * at a given selection point
 * @param {object} input - the input element to obtain coordinates for
 * @param {number} selectionPoint - the selection point for the input
 */
// https://jh3y.medium.com/how-to-where-s-the-caret-getting-the-xy-position-of-the-caret-a24ba372990a
fun getCursorXY(input: HTMLElement, selectionPoint: Int): CursorXY {
    val inputX = input.offsetLeft
    val inputY = input.offsetTop

    // create a dummy element that will be a clone of our input
    document.querySelector("#hacky")?.remove()
    val div = document.createElement("div") as HTMLElement
    div.id = ElementId("hacky")

    // get the computed style of the input and clone it onto the dummy element
    val copyStyle = getComputedStyle(input)

    for (prop in copyStyle.asList()) {
        div.style.setProperty(prop, copyStyle.getPropertyValue(prop))
    }

    div.style.setProperty("position", "absolute")
    div.style.setProperty("z-index", "10000")
    div.style.setProperty("visibility", "hidden")

    // we need a character that will replace whitespace when filling our dummy element if it's a single line <input/>
    val swap = "."
    val inputValue = when (input) {
        is HTMLInputElement -> {
            input.value.replace(" ", swap)
        }

        is HTMLTextAreaElement -> {
            input.value
        }

        else -> error("Unsupported element $input")
    }

    // set the div content to that of the textarea up until selection
    val textContent = inputValue.substring(0, selectionPoint)
    // set the text content of the dummy element div
    div.textContent = textContent

    if (input.tagName == "TEXTAREA") {
        div.style.height = "auto"
    }

    // if a single line input then the div needs to be single line and not break out like a text area
    if (input.tagName == "INPUT") {
        div.style.width = "auto"
    }

    // create a marker element to obtain caret position
    val span = document.createElement("span") as HTMLElement
    // give the span the textContent of remaining content so that the recreated dummy element is as close as possible
    span.textContent = inputValue.substring(selectionPoint).ifEmpty { "." }
    // append the span marker to the div
    div.appendChild(span)
    // append the dummy element to the body
    document.body.appendChild(div)
    // get the marker position, this is the caret position top and left relative to the input
    val spanX = span.offsetLeft
    val spanY = span.offsetTop
    // lastly, remove that dummy element
    // NOTE:: can comment this out for debugging purposes if you want to see where that span is rendered
    document.body.removeChild(div)
    // return an object with the x and y of the caret. account for input positioning so that you don't need to wrap the input

    // Power Changes: Subtract the scroll offset to the cursor position, fixes issues when the textarea scrolls
    return CursorXY(inputX + spanX - input.scrollLeft.toInt(), inputY + spanY - input.scrollTop.toInt())
}

data class CursorXY(val x: Int, val y: Int)