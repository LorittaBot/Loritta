package net.perfectdreams.spicymorenitta.utils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.get
import kotlin.browser.document

val page = BetterDocument(document)

class BetterDocument(val document: Document) {
	fun getElementById(name: String): Element {
		return document.getElementById(name)!!
	}

	fun getElementByClass(name: String): Element {
		return document.getElementsByClassName(name)[0]!!
	}

	fun <T> getElementById(name: String): T {
		return document.getElementById(name)!! as T
	}
}

fun Element.appendBuilder(builder: StringBuilder) {
	val elChild = document.createElement("div")
	elChild.innerHTML = builder.toString()
	appendChild(elChild)
}