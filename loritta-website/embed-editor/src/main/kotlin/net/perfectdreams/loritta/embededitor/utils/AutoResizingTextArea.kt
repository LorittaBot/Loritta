package net.perfectdreams.loritta.embededitor.utils

import org.w3c.dom.HTMLTextAreaElement

fun HTMLTextAreaElement.autoResize() {
    this.style.asDynamic().overflow = "hidden"

    this.addEventListener("input", { event ->
        val textArea = (event.target as HTMLTextAreaElement)
        resizeToContent(textArea)
    })

    resizeToContent(this)
}

fun HTMLTextAreaElement.resizeToContent(textArea: HTMLTextAreaElement) {
    this.style.asDynamic().overflow = "hidden"
    textArea.style.height = "auto"
    textArea.style.height = textArea.scrollHeight.toString() + "px"
}