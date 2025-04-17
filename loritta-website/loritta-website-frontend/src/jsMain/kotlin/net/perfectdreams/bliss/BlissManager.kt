package net.perfectdreams.bliss

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import net.perfectdreams.loritta.website.frontend.LorittaWebsiteFrontend
import net.perfectdreams.loritta.website.frontend.utils.extensions.select
import net.perfectdreams.loritta.website.frontend.utils.extensions.selectAll
import web.dom.ParentNode
import web.dom.document
import web.events.Event
import web.events.EventType
import web.events.addEventListener
import web.history.history
import web.html.HTMLDivElement
import web.html.HTMLElement
import web.parsing.DOMParser
import web.parsing.DOMParserSupportedType
import web.window.window

object BlissManager {
    private val SWAP_PATTERN = Regex("(?<sourceSelector>[A-Za-z0-9-_#.]+) -> (?<targetSelector>[A-Za-z0-9-_#.]+)(?<traits> [A-Za-z0-9:.]+)?")
    private val http = HttpClient {}

    // TODO: We should not depend on LorittaWebsiteFrontend, maybe add events?
    fun setup(m: LorittaWebsiteFrontend, element: ParentNode) {
        val blissGetElements = element.selectAll<HTMLDivElement>("[bliss-get]")

        for (element in blissGetElements) {
            if (element.asDynamic().isBliss)
                continue // Already happy :3

            element.asDynamic().isBliss = true

            println("The element $element has been blessed :3")

            val unparsedTriggers = element.getAttribute("bliss-trigger") ?: "click"

            val triggers = unparsedTriggers.split(",")
                .map { it.trim() }

            for (trigger in triggers) {
                if (trigger == "mount") {
                    m.launchGlobal {
                        executeBlissRequest(m, element)
                    }
                } else {
                    element.addEventListener(
                        EventType<Event>(trigger),
                        {
                            it.preventDefault()

                            m.launchGlobal {
                                executeBlissRequest(m, element)
                            }
                        }
                    )
                }
            }
        }
    }

    private suspend fun executeBlissRequest(m: LorittaWebsiteFrontend, element: HTMLElement) {
        val actionUrl: String

        val getUrl = element.getAttribute("bliss-get")!!
        val pushUrlAttribute = element.getAttribute("bliss-push-url")

        if (getUrl.startsWith("[") && getUrl.endsWith("]")) {
            actionUrl = element.getAttribute(getUrl.removePrefix("[").removeSuffix("]"))!!
        } else {
            actionUrl = getUrl
        }

        m.startFakeProgressIndicator()

        val response = http.get(actionUrl) {
            header("Bliss-Triggered-By-Id", element.id)
        }

        val blissRedirectHeader = response.headers["Bliss-Redirect"]
        if (blissRedirectHeader != null) {
            // Bye bye! If we received this header, we don't need to parse anything else
            document.location.href = blissRedirectHeader
            return
        }

        val body = response.bodyAsText()

        val parser = DOMParser()
        val newDocument = parser.parseFromString(body, DOMParserSupportedType.textHtml)

        // #left-sidebar-entries -> #left-sidebar-entries
        // #right-sidebar -> #right-sidebar

        // Copying preserved elements is a bit tricky, depending on the swap target
        // If we have a swap "#left-sidebar-entries -> #left-sidebar-entries" but the document HAS a bliss-preserve element, the bliss-preserve
        // element would've been removed from the DOM, because we did "replaceWith" on our newDocument (which removes the element from the DOM)
        // Let's copy all preserved elements
        // First off, on the new document (the document we retrieved), do we have any elements marked to be preserved?
        val preservedElements = mutableListOf<HTMLElement>()
        val elementsMarkedAsPreservedOnNewDocument = newDocument.selectAll<HTMLDivElement>("[bliss-preserve]")
        for (element in elementsMarkedAsPreservedOnNewDocument) {
            if (element.id.isEmpty())
                error("Element is marked to be preserved, but it does not have any ID associated with it!")

            val originalElement = document.body.select<HTMLElement?>("#${element.id}")
            if (originalElement != null) {
                preservedElements.add(originalElement)
            }
        }

        // Parse the bliss-swaps
        val swaps = element.getAttribute("bliss-swaps")!!
        val swapLines = swaps.split("\n").map { it.trim() }

        for (swapEntry in swapLines) {
            val match = SWAP_PATTERN.matchEntire(swapEntry) ?: error("Invalid swap entry! \"$swapEntry\"")
            val source = match.groups["sourceSelector"]!!.value
            val target = match.groups["targetSelector"]!!.value
            val traitValues = parseTraits(match.groups["traits"]?.value)

            val swapType = traitValues["swapType"] ?: "innerHTML"

            val sourceElement = newDocument.select<HTMLElement?>(source) ?: error("Could not find source element \"$source\"!")

            // We need to use select on the document to be able to find body references
            val targetElement = if (target == "self") {
                element
            } else {
                document.select<HTMLElement?>(target) ?: error("Could not find target element \"$target\"!")
            }

            for (preservedElement in preservedElements) {
                val elementToBeReplaced = sourceElement.select<HTMLElement?>("#" + preservedElement.id) ?: continue
                elementToBeReplaced.replaceWith(preservedElement)
            }

            // Setup the source element before replacing
            setup(m, sourceElement)
            m.mountComponents(sourceElement)

            if (swapType == "innerHTML") {
                // Clear everything
                targetElement.innerHTML = ""

                // Append the children
                while (true) {
                    val child = sourceElement.firstChild ?: break
                    targetElement.appendChild(child)
                }
            } else if (swapType == "outerHTML") {
                // Currently this is outerHTML
                targetElement.replaceWith(sourceElement)
            } else error("Unknown swap type! $swapType")
        }

        // Update page title if present
        if (!newDocument.title.isEmpty()) {
            document.title = newDocument.title
        }

        if (pushUrlAttribute != null) {
            val pushUrl = if (pushUrlAttribute.startsWith("[") && pushUrlAttribute.endsWith("]")) {
                element.getAttribute(pushUrlAttribute.removePrefix("[").removeSuffix("]"))!!
            } else {
                pushUrlAttribute
            }

            history.pushState(null, "", pushUrl)
        }

        val afterAttributes = element.getAttribute("bliss-after")
        if (afterAttributes != null) {
            val afterTraits = parseTraits(afterAttributes)
            val scrollValue = afterTraits["scroll"]
            if (scrollValue == "window:top") {
                // Scroll to the top!
                window.scrollTo(0.0, 0.0)
            }
        }

        m.stopFakeProgressIndicator()
    }

    private fun parseTraits(unparsedTraits: String?): Map<String, String> {
        if (unparsedTraits == null)
            return emptyMap()

        val traitValues = mutableMapOf<String, String>()

        println("Traits: $unparsedTraits")
        for (entry in unparsedTraits.split(Regex(" +")).filter { it.isNotBlank() }) {
            println("Parsing trait $entry...")
            val key =  entry.substringBefore(":")
            val value = entry.substringAfter(":")
            traitValues[key] = value
        }

        return traitValues
    }
}