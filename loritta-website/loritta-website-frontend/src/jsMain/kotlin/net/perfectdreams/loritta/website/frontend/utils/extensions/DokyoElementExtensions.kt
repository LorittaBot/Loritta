package net.perfectdreams.loritta.website.frontend.utils.extensions

import net.perfectdreams.dokyo.elements.PageElement
import web.dom.document
import web.html.HTMLDivElement

fun <T> PageElement.get() = document.getElementById(this.id) as T
fun PageElement.get() = document.getElementById(this.id) as HTMLDivElement
