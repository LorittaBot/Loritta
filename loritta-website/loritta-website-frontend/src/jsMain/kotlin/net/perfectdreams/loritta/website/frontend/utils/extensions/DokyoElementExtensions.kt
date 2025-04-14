package net.perfectdreams.loritta.website.frontend.utils.extensions

import kotlinx.browser.document
import net.perfectdreams.dokyo.elements.PageElement
import org.w3c.dom.HTMLDivElement

fun <T> PageElement.get() = document.getElementById(this.id) as T
fun PageElement.get() = document.getElementById(this.id) as HTMLDivElement
