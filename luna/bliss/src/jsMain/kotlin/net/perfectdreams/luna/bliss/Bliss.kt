@file:OptIn(ExperimentalEncodingApi::class)

package net.perfectdreams.luna.bliss

import js.array.asList
import js.objects.unsafeJso
import js.typedarrays.Int8Array
import js.typedarrays.asByteArray
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import web.blob.arrayBuffer
import web.cssom.ClassName
import web.dom.Document
import web.dom.Element
import web.dom.ElementId
import web.dom.document
import web.events.CHANGE
import web.events.CustomEvent
import web.events.CustomEventInit
import web.events.Event
import web.events.EventHandler
import web.events.EventInit
import web.events.EventType
import web.events.addEventHandler
import web.history.POP_STATE
import web.history.PopStateEvent
import web.history.history
import web.html.HTMLAnchorElement
import web.html.HTMLDivElement
import web.html.HTMLInputElement
import web.html.HTMLOptionElement
import web.html.HTMLScriptElement
import web.html.HTMLSelectElement
import web.html.HTMLTextAreaElement
import web.html.InputType
import web.html.checkbox
import web.html.file
import web.html.radio
import web.http.BodyInit
import web.http.Headers
import web.http.RequestInit
import web.http.fetch
import web.http.text
import web.input.INPUT
import web.input.InputEvent
import web.input.InputEventInit
import web.location.location
import web.navigator.navigator
import web.parsing.DOMParser
import web.parsing.DOMParserSupportedType
import web.parsing.textHtml
import web.pointer.CLICK
import web.pointer.PointerEvent
import web.sse.EventSource
import web.url.URL
import web.window.window
import kotlin.collections.iterator
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.text.get

// I find I'm here this place of bliss
object Bliss {
    private val SWAP_REGEX = Regex("(?<sourceQuerySelector>[A-Za-z#.\\-0-9]+)( \\((?<sourceSwapType>[A-Za-z]+)\\))? -> (?<targetQuerySelector>[A-Za-z#.\\-0-9]+)( \\((?<targetSwapType>[A-Za-z]+)\\))?")
    private val DISABLE_WHEN_REGEX = Regex("(?<querySelector>.+) (?<op>==|!=) (?<part>\"(?<text>.+)\"|(?<blank>blank)|(?<empty>empty))")
    private val supportsMoveBeforeAPI = js("typeof Element !== 'undefined' && 'moveBefore' in Element.prototype") as Boolean

    private val methods = setOf(
        HttpMethod.Get,
        HttpMethod.Post,
        HttpMethod.Put,
        HttpMethod.Patch,
        HttpMethod.Delete
    )

    private val componentBuilders = mutableMapOf<String, () -> (BlissComponent<*>)>()
    private val elementProcessors = mutableListOf<(Element) -> (Unit)>()

    fun setupEvents() {
        // We need this to make the pop state below work correctly on the first visited page!
        history.replaceState(
            unsafeJso<dynamic> {
                this.blessed = true
            },
            "",
            window.location.href
        )

        window.addEventHandler(PopStateEvent.POP_STATE) {
            // We will only reload the page if the state has generated from our "blessed" links
            // This fixes an issue with hash "jump to" links causing a page refresh
            if (it.state != null && it.state.asDynamic().blessed == true) {
                // When pressing the back button, reload the entire page to avoid broken states
                window.location.reload()
            }
        }
    }

    fun processAttributes(element: Element) {
        if (!element.asDynamic().blissBlessed) {
            element.asDynamic().blissBlessed = true

            for (processor in elementProcessors) {
                processor(element)
            }

            processCopyTextOnClick(element)
            processMirrorValueToElement(element)
            processBlissComponents(element)

            for (method in methods) {
                val requestMethodUrl = element.getAttribute("bliss-${method.value.lowercase()}") ?: continue

                val triggers = element.getAttribute("bliss-trigger")?.split(",")?.map { it.trim() } ?: listOf("click")
                val sync = element.getAttribute("bliss-sync")
                val includeQuery = element.getAttribute("bliss-include-query")
                val includeJson = element.getAttribute("bliss-include-json")
                val valsQuery = element.getAttribute("bliss-vals-query")
                val valsJson = element.getAttribute("bliss-vals-json")
                val remapJsonKeys = element.getAttribute("bliss-remap-json-keys")
                val headers = element.getAttribute("bliss-headers")
                val indicator = element.getAttribute("bliss-indicator")?.split(",")?.map { it.trim() }

                suspend fun executeHttp() {
                    val baseUrl = if (requestMethodUrl.startsWith("[")) {
                        element.getAttribute(requestMethodUrl.removePrefix("[").removeSuffix("]")) ?: error("Missing element attribute!")
                    } else {
                        requestMethodUrl
                    }

                    val swaps = mutableListOf<SwapRequest>()

                    // We do this way because we can select the responses based on the status response, sweet!
                    // The default will be "nothing", to avoid weird errors (like client or server errors) being swapped to the DOM
                    val swapAttributes = element.attributes.asList().filter { it.name.startsWith("bliss-swap:") }

                    for (swapAttribute in swapAttributes) {
                        // This seems weird, but that's because there are some hefty restrictions for attribute names
                        // So the format is "bliss-swap:200_201_202_203", where _ is like a ,
                        val validCodes = swapAttribute.name.substringAfterLast(":").split("_").map { it.toInt() }.toSet()

                        swaps.add(
                            SwapRequest(
                                validCodes.map { HttpStatusCode.fromValue(it) }.toSet(),
                                swapAttribute.value
                            )
                        )
                    }

                    val pushUrls = mutableListOf<PushUrlRequest>()

                    // We do this way because we can select the responses based on the status response, sweet!
                    // The default will be "nothing", to avoid weird errors (like client or server errors) being pushed
                    val matchedAttributes = element.attributes.asList().filter { it.name.startsWith("bliss-push-url:") }

                    for (attribute in matchedAttributes) {
                        // This seems weird, but that's because there are some hefty restrictions for attribute names
                        // So the format is "bliss-swap:200_201_202_203", where _ is like a ,
                        val validCodes = attribute.name.substringAfterLast(":").split("_").map { it.toInt() }.toSet()

                        pushUrls.add(
                            PushUrlRequest(
                                validCodes.map { HttpStatusCode.fromValue(it) }.toSet(),
                                attribute.value
                            )
                        )
                    }

                    executeAjax(
                        element,
                        method,
                        baseUrl,
                        headers?.let { Json.decodeFromString<Map<String, String>>(it) } ?: emptyMap(),
                        includeQuery,
                        includeJson,
                        valsQuery?.let { Json.decodeFromString<Map<String, JsonPrimitive>>(it) } ?: emptyMap(),
                        valsJson?.let { Json.decodeFromString<Map<String, JsonPrimitive>>(it) } ?: emptyMap(),
                        remapJsonKeys?.let { Json.decodeFromString<Map<String, String>>(it) } ?: emptyMap(),
                        swaps,
                        pushUrls
                    )
                }

                fun prepareAndExecuteHttp(disableElement: Boolean) {
                    // If the element is marked as aria-disabled, we'll ignore it (used for anchor links)
                    if (element.getAttribute("aria-disabled") == "true")
                        return

                    val detail = BlissBeforeBlissRequestPrepare(element)

                    val event = CustomEvent(
                        type = EventType("bliss:beforeBlissRequestPrepare"),
                        init = CustomEventInit(detail = detail, cancelable = true)
                    )

                    document.dispatchEvent(event)

                    println("Default prevented? ${event.defaultPrevented}")
                    if (event.defaultPrevented)
                        return

                    println("Should we disable the element? $disableElement")
                    if (disableElement) {
                        if (element is HTMLAnchorElement) {
                            element.setAttribute("aria-disabled", "true")
                        } else {
                            element.setAttribute("disabled", "")
                        }
                    }

                    val indicatorElements = mutableListOf<Element>()
                    for (indicatorQuery in indicator ?: listOf()) {
                        val indicators = if (indicatorQuery == "this")
                            listOf(element)
                        else document.querySelectorAll(indicatorQuery).asList()
                        indicatorElements.addAll(indicators)
                    }

                    for (element in indicatorElements) {
                        element.classList.add(ClassName("bliss-request"))
                    }

                    val syncManagerElementTarget = if (sync != null) {
                        document.querySelector(sync) ?: error("Could not find element $sync!")
                    } else {
                        element
                    }

                    // Currently we only support keeping one request in flight, in the future it would be nice to support a request queue!
                    val manager = if (syncManagerElementTarget.asDynamic().blissRequestManager == undefined) {
                        val manager = BlissRequestManager()
                        syncManagerElementTarget.asDynamic().blissRequestManager = manager
                        manager
                    } else syncManagerElementTarget.asDynamic().blissRequestManager as BlissRequestManager

                    manager.inflight?.cancel()
                    manager.inflight = GlobalScope.launch {
                        executeHttp()

                        for (indicatorElement in indicatorElements) {
                            indicatorElement.classList.remove(ClassName("bliss-request"))
                        }

                        if (element is HTMLAnchorElement) {
                            element.removeAttribute("aria-disabled")
                        } else {
                            element.removeAttribute("disabled")
                        }
                    }
                }

                for (triggerName in triggers) {
                    when (triggerName) {
                        "click" -> {
                            element.addEventHandler(PointerEvent.CLICK) {
                                it.preventDefault()

                                prepareAndExecuteHttp(true)
                            }
                        }

                        "input" -> {
                            element.addEventHandler(InputEvent.INPUT) {
                                prepareAndExecuteHttp(false)
                            }
                        }

                        "load" -> {
                            prepareAndExecuteHttp(true)
                        }
                    }
                }
            }

            val sse = element.getAttribute("bliss-sse")
            if (sse != null) {
                val es = EventSource(sse)

                element.whenRemovedFromDOM {
                    println("SSE element has been removed from the DOM, closing connection...")
                    es.close()
                }

                es.onopen = EventHandler {
                    println("SSE open!")
                }

                es.onmessage = EventHandler { event ->
                    println("Received message: ${event.data}")

                    val sseEvent = Json.decodeFromString<SSEBliss>(event.data as String)

                    when (sseEvent) {
                        is SSEBlissSwap -> {
                            val parser = DOMParser()
                            val doc = parser.parseFromString(sseEvent.content, DOMParserSupportedType.textHtml)

                            val didSwap = doSwap(
                                sseEvent.swap,
                                element,
                                sourceDocument = doc,
                                targetDocument = document
                            )

                            if (didSwap) {
                                processAttributes(document.body)
                            }
                        }

                        is SSECustomEvent -> {
                            val query = if (sseEvent.eventTarget == "window")
                                listOf(window)
                            else if (sseEvent.eventTarget == "document")
                                listOf(document)
                            else
                                document.querySelectorAll(sseEvent.eventTarget).asList()

                            if (query.isNotEmpty()) {
                                val event = CustomEvent(
                                    type = EventType(sseEvent.event),
                                    init = CustomEventInit(
                                        detail = sseEvent.content,
                                        bubbles = true, // let it bubble up the DOM
                                        composed = true // cross-shadow DOM boundary if needed
                                    )
                                )

                                for (element in query) {
                                    element.dispatchEvent(event)
                                }
                            }
                        }
                    }
                }
            }

            val disableWhenAttribute = element.getAttribute("bliss-disable-when")

            if (disableWhenAttribute != null) {
                println("BLISS DISABLE WHEN!!! $disableWhenAttribute")

                val operatorRegex = Regex("\\s*(\\|\\||&&)\\s*")
                val parts = disableWhenAttribute.split(operatorRegex)
                val operators = operatorRegex.findAll(disableWhenAttribute).map { it.groupValues[1] }.toList()

                data class ConditionCheck(val element: Element, val check: () -> Boolean)

                val conditions = mutableListOf<ConditionCheck>()

                for (entry in parts) {
                    val match = DISABLE_WHEN_REGEX.matchEntire(entry.trim()) ?: error("Failed to match $entry!")

                    val selector = match.groups["querySelector"]!!.value
                    val text = match.groups["text"]?.value
                    val blank = match.groups["blank"]?.value
                    val empty = match.groups["empty"]?.value
                    val op = match.groups["op"]?.value
                    val expectedBoolValue = op == "=="

                    println("Registering $selector for disabled when (text: $text, blank: $blank, empty: $empty)")
                    val targetEventElement = document.querySelector(selector) ?: error("Could not find element $selector!")

                    val condition: () -> Boolean = if (targetEventElement is HTMLInputElement) {
                        if (empty != null) {
                            { targetEventElement.value.isEmpty() == expectedBoolValue }
                        } else if (blank != null) {
                            { targetEventElement.value.isBlank() == expectedBoolValue }
                        } else if (text != null) {
                            { (targetEventElement.value == text) == expectedBoolValue }
                        } else {
                            error("Invalid condition for input element!")
                        }
                    } else if (targetEventElement is HTMLTextAreaElement) {
                        if (empty != null) {
                            { targetEventElement.value.isEmpty() == expectedBoolValue }
                        } else if (blank != null) {
                            { targetEventElement.value.isBlank() == expectedBoolValue }
                        } else if (text != null) {
                            { (targetEventElement.value == text) == expectedBoolValue }
                        } else {
                            error("Invalid condition for textarea element!")
                        }
                    } else if (targetEventElement is HTMLSelectElement) {
                        if (text != null) {
                            { (targetEventElement.value == text) == expectedBoolValue }
                        } else {
                            error("Invalid condition for select element!")
                        }
                    } else {
                        error("Cannot target element $targetEventElement for disabled when!")
                    }

                    conditions.add(ConditionCheck(targetEventElement, condition))
                }

                fun checkDisableCondition() {
                    val values = conditions.map { it.check() }.toMutableList()
                    val currentOps = operators.toMutableList()

                    // Operator precedence: && first
                    var i = 0
                    while (i < currentOps.size) {
                        if (currentOps[i] == "&&") {
                            val left = values[i]
                            val right = values[i + 1]
                            values[i] = left && right
                            values.removeAt(i + 1)
                            currentOps.removeAt(i)
                        } else {
                            i++
                        }
                    }

                    // Then ||
                    var result = values[0]
                    for (j in 0 until currentOps.size) {
                        result = result || values[j + 1]
                    }

                    if (result) {
                        element.setAttribute("disabled", "")
                    } else {
                        element.removeAttribute("disabled")
                    }
                }

                for (condition in conditions) {
                    val targetEventElement = condition.element
                    if (targetEventElement is HTMLInputElement || targetEventElement is HTMLTextAreaElement) {
                        targetEventElement.addEventHandler(InputEvent.INPUT) {
                            checkDisableCondition()
                        }
                    } else if (targetEventElement is HTMLSelectElement) {
                        targetEventElement.addEventHandler(Event.CHANGE) {
                            checkDisableCondition()
                        }
                    }
                }

                checkDisableCondition()
            }

            val transformText = element.getAttribute("bliss-transform-text")
            if (transformText != null) {
                val entries = transformText.split(",").map { it.trim() }

                if (element is HTMLInputElement) {
                    if (entries.isNotEmpty()) {
                        element.addEventHandler(InputEvent.INPUT) {
                            var newValue = element.value

                            if (entries.contains("uppercase")) {
                                newValue = newValue.uppercase()
                            }

                            if (entries.contains("lowercase")) {
                                newValue = newValue.lowercase()
                            }

                            if (entries.contains("trim")) {
                                newValue = newValue.trim()
                            }

                            if (entries.contains("no-spaces")) {
                                newValue = newValue.replace(" ", "")
                            }

                            element.value = newValue
                        }
                    }
                }
            }
        }

        // Kick it root down!
        for (child in element.children.asList()) {
            processAttributes(child)
        }
    }

    fun registerComponent(componentId: String, construct: () -> (BlissComponent<*>)) {
        this.componentBuilders[componentId] = construct
    }

    fun registerElementProcessor(processor: (Element) -> (Unit)) {
        this.elementProcessors.add(processor)
    }

    fun registerDocumentParsedEventListener(processor: (Document) -> (Unit)) {
        document.addEventHandler(EventType<CustomEvent<BlissDocumentParsed>>("bliss:documentParsed")) {
            processor.invoke(it.detail.document)
        }
    }

    fun doSwap(
        swapValue: String,
        element: Element,
        sourceDocument: Document,
        targetDocument: Document
    ): Boolean {
        // And finally... let's parse and execute the swap!
        val swaps = swapValue.split(",").map { it.trim() }

        var didSwap = false

        val pantryElement = targetDocument.createElement("div").apply {
            this.id = ElementId("--bliss-preserve-pantry--")
        } as HTMLDivElement

        // Yes, we need to append to the document
        targetDocument.body.appendChild(pantryElement)

        // Check which elements should be preserved
        val elementsToBePreserved = mutableListOf<Element>()

        for (entry in swaps) {
            println("Swap entry: \"$entry\"")
            // "nothing" is special: it literally does nothing, useful for things that you expect the server to return a redirect
            if (entry != "nothing") {
                val result = SWAP_REGEX.matchEntire(entry) ?: error("Invalid swap setting ${entry}!")
                val sourceQuerySelector = result.groups["sourceQuerySelector"]!!.value
                val targetQuerySelector = result.groups["targetQuerySelector"]!!.value

                val sourceSwapType = result.groups["sourceSwapType"]?.value ?: "outerHTML"
                val targetSwapType = result.groups["targetSwapType"]?.value ?: "outerHTML"

                val sourceElement = sourceDocument.querySelector(sourceQuerySelector) ?: error("Could not find element $sourceQuerySelector!")
                val targetElement = if (targetQuerySelector == "this")
                    element
                else targetDocument.querySelector(targetQuerySelector) ?: error("Could not find element $targetQuerySelector!")

                val sourceElements = when (sourceSwapType) {
                    "outerHTML" -> listOf(sourceElement)
                    "innerHTML" -> sourceElement.childNodes.asList()
                    else -> error("Unknown swap type: $sourceSwapType")
                }

                val clonedSourceElements = sourceElements.map { it.cloneNode(true) }
                val clonedSourceElementsCopy = clonedSourceElements.toMutableList()

                // Before we actually swap, we need to check which elements are marked as preserved in the target!
                val elementsToBePreservedThatAreInsideOfTarget = targetElement.querySelectorAll("[bliss-preserve='true']").asList()

                // We need to keep the preserved elements in the DOM, to avoid them being removed by the browser and causing issues when using moveBefore
                // (That's how htmx solves this issue :3)
                for (element in elementsToBePreservedThatAreInsideOfTarget) {
                    if (this.supportsMoveBeforeAPI) {
                        pantryElement.asDynamic().moveBefore(element, null)
                        elementsToBePreserved.add(element)
                    }
                }

                // We need to use child nodes here, to avoid responses that only have text nodes being "ignored"
                when (targetSwapType) {
                    "outerHTML" -> targetElement.replaceWith(*clonedSourceElements.toTypedArray())
                    "innerHTML" -> targetElement.replaceChildren(*clonedSourceElements.toTypedArray())
                    else -> error("Unknown swap type: $targetSwapType")
                }

                // Execute any scripts that was swapped
                for (swappedElement in clonedSourceElementsCopy) {
                    if (swappedElement is Element) {
                        val scriptElements = swappedElement.querySelectorAll("script").asList()
                            .toMutableList()

                        if (swappedElement is HTMLScriptElement) {
                            scriptElements.add(swappedElement)
                        }

                        for (element in scriptElements) {
                            if (element is HTMLScriptElement) {
                                if (element.type == "text/javascript" || element.type == "") {
                                    // We use asDynamic to invoke the eval function due to this bug: https://kotlinlang.slack.com/archives/C0B8L3U69/p1715660256035359
                                    // Last tested: 27/10/2025
                                    try {
                                        window.asDynamic().eval(element.innerHTML)
                                    } catch (e: Throwable) {
                                        println("Something went wrong while evalutating a swapped script!")
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                }

                val componentsInTheDOM = document.querySelectorAll("[bliss-component]").asList()
                for (component in componentsInTheDOM) {
                    val blissComponents = component.blissComponents

                    for (sourceElement in clonedSourceElementsCopy) {
                        // The source element may be something like a TextNode, so we need to check if it is actually an element
                        if (sourceElement is Element) {
                            for (blissComponent in blissComponents) {
                                blissComponent.onElementSwap(sourceElement)
                            }
                        }
                    }
                }

                didSwap = true
            }
        }

        if (didSwap) {
            for (elementToBePreserved in elementsToBePreserved) {
                val targetToBeReplaced = targetDocument.body.querySelector("#${elementToBePreserved.id}:not(#--bliss-preserve-pantry-- *)\n")

                if (targetToBeReplaced != null) {
                    if (this.supportsMoveBeforeAPI) {
                        // Yes, the moveBefore should be called by the PARENT of the element!
                        targetToBeReplaced.parentElement?.moveBefore(elementToBePreserved, targetToBeReplaced)
                        targetToBeReplaced.remove() // bye!
                    }
                }
            }
        }

        // Remove the pantry from the DOM
        pantryElement.remove()

        return didSwap
    }

    private fun processCopyTextOnClick(element: Element) {
        val copyTextOnClick = element.getAttribute("bliss-copy-text-on-click")

        if (copyTextOnClick != null) {
            element.addEventHandler(PointerEvent.CLICK) {
                navigator.clipboard.writeTextAsync(copyTextOnClick)
            }
        }
    }

    /**
     * Mirrors the value of the [element] to the query selector
     */
    private fun processMirrorValueToElement(element: Element) {
        val mirrorValue = element.getAttribute("bliss-mirror-value-to-element")

        if (mirrorValue != null) {
            fun setValue(newValue: String) {
                val targetElement = document.querySelector(mirrorValue) ?: error("Could not find element $mirrorValue!")

                when (targetElement) {
                    is HTMLInputElement -> {
                        targetElement.value = newValue
                    }

                    is HTMLTextAreaElement -> {
                        targetElement.value = newValue
                    }

                    is HTMLOptionElement -> {
                        targetElement.value = newValue
                    }

                    else -> error("Unsupported target element!")
                }

                targetElement.dispatchEvent(InputEvent(InputEvent.INPUT, InputEventInit(bubbles = true)))
                targetElement.dispatchEvent(Event(Event.CHANGE, EventInit(bubbles = true)))
            }

            when (element) {
                is HTMLInputElement -> {
                    element.addEventHandler(InputEvent.INPUT) {
                        setValue(element.value)
                    }
                }

                is HTMLTextAreaElement -> {
                    element.addEventHandler(InputEvent.INPUT) {
                        setValue(element.value)
                    }
                }

                is HTMLOptionElement -> {
                    element.addEventHandler(InputEvent.INPUT) {
                        setValue(element.value)
                    }
                }

                else -> error("Unsupported source type!")
            }
        }
    }

    private fun processBlissComponents(element: Element) {
        val componentIds = element.getAttribute("bliss-component")
            ?.split(",")
            ?.map { it.trim() } ?: emptyList()

        for (componentId in componentIds) {
            val createdComponents = mutableListOf<BlissComponent<*>>()

            try {
                val componentConstructor = componentBuilders[componentId] ?: error("Could not find component $componentId!")
                val component = componentConstructor.invoke()
                createdComponents.add(component)
                component.mount(element)

                element.whenRemovedFromDOM {
                    component.unmount()
                }
            } catch (e: Throwable) {
                println("Something went wrong while trying to setup component \"$componentId\"! Skipping...")
                e.printStackTrace()
            }

            if (createdComponents.isNotEmpty()) {
                element.blissComponents = createdComponents
            }
        }
    }

    /**
     * Creates a map of [JsonElement] based on the named elements of [includeElements]
     *
     * @param keyAttributeName the name of the attribute that will be used as the key in the map, if not present in the element, it will be ignored
     * @param includeElements  what elements should be included in the query
     */
    suspend fun createMapOfElementValues(
        keyAttributeName: String,
        includeElements: List<Element>,
        remapKeys: Map<String, String>
    ): MutableMap<String, JsonElement> {
        fun remap(key: String): String {
            return remapKeys[key] ?: key
        }

        val json = mutableMapOf<String, JsonElement>()

        for (includeElement in includeElements) {
            fun setOrCreateList(key: String, newValue: JsonElement) {
                val isArray = key.endsWith("[]")
                if (isArray) {
                    val nameWithoutBrackets = remap(key.removeSuffix("[]"))

                    if (json.containsKey(nameWithoutBrackets)) {
                        val currentList = json[nameWithoutBrackets] as? JsonArray ?: error("Element $key is already present on the JSON, but it isn't an JsonArray! Bug?")

                        json[nameWithoutBrackets] = buildJsonArray {
                            for (value in currentList) {
                                add(value)
                            }
                            add(newValue)
                        }
                    } else {
                        json[nameWithoutBrackets] = buildJsonArray {
                            add(newValue)
                        }
                    }
                } else {
                    json[remap(key)] = newValue
                }
            }

            suspend fun processElementValue(element: Element) {
                // Skip if the element does not have our desired attribute...
                val keyName = element.getAttribute(keyAttributeName) ?: return
                // Also skip if the attribute exists but is blank
                if (keyName.isBlank())
                    return

                if (element is HTMLInputElement) {
                    if (element.type == InputType.file) {
                        val files = element.files!!.asList()

                        val array = mutableListOf<JsonObject>()

                        for (file in files) {
                            // DO NOT USE file.bytes().toByteArray() because THAT DOES NOT WORK IN CHROMIUM BROWSERS!!
                            val fileAsBytes = Int8Array(file.arrayBuffer())
                                .asByteArray()
                            val fileName = file.name

                            array.add(
                                buildJsonObject {
                                    put("data", Base64.encode(fileAsBytes))
                                    put("name", fileName)
                                }
                            )
                        }

                        setOrCreateList(
                            keyName,
                            buildJsonArray {
                                for (entry in array) {
                                    add(entry)
                                }
                            }
                        )
                    } else if (element.type == InputType.checkbox) {
                        setOrCreateList(
                            keyName,
                            JsonPrimitive(element.checked)
                        )
                    } else if (element.type == InputType.radio) {
                        if (element.checked) {
                            setOrCreateList(
                                keyName,
                                JsonPrimitive(element.value)
                            )
                        }
                    } else {
                        var value: String? = element.value
                        if (value.isNullOrBlank() && element.getAttribute("bliss-coerce-to-null-if-blank") == "true")
                            value = null

                        if (value != null && element.getAttribute("bliss-parse-to-json") == "true") {
                            setOrCreateList(
                                keyName,
                                Json.parseToJsonElement(value)
                            )
                        } else {
                            setOrCreateList(
                                keyName,
                                JsonPrimitive(value)
                            )
                        }
                    }
                } else if (element is HTMLSelectElement) {
                    var value: String? = element.value
                    if (value.isNullOrBlank() && element.getAttribute("bliss-coerce-to-null-if-blank") == "true")
                        value = null

                    setOrCreateList(
                        keyName,
                        JsonPrimitive(value)
                    )
                } else if (element is HTMLTextAreaElement) {
                    var value: String? = element.value
                    if (value.isNullOrBlank() && element.getAttribute("bliss-coerce-to-null-if-blank") == "true")
                        value = null

                    setOrCreateList(
                        keyName,
                        JsonPrimitive(value)
                    )
                }
            }

            if (includeElement is HTMLInputElement || includeElement is HTMLSelectElement || includeElement is HTMLTextAreaElement) {
                processElementValue(includeElement)
            } else {
                val namedElements = includeElement.querySelectorAll("[$keyAttributeName]")

                for (element in namedElements.asList()) {
                    processElementValue(element)
                }
            }
        }

        return json
    }

    suspend fun executeAjax(
        sourceElement: Element?,
        method: HttpMethod,
        baseUrl: String,
        headers: Map<String, String>,
        includeQuery: String?,
        includeJson: String?,
        valsQuery: Map<String, JsonElement>,
        valsJson: Map<String, JsonElement>,
        remapJsonKeys: Map<String, String>,
        swaps: List<SwapRequest>,
        pushUrls: List<PushUrlRequest>,
    ) {
        val requestUrl = URL(baseUrl, window.location.origin).apply {
            if (includeQuery != null) {
                val querySelectors = includeQuery.split(",").map { it.trim() }

                for (selector in querySelectors) {
                    val includeElement = document.querySelector(selector) ?: error("Could not find element $selector!")

                    if (includeElement is HTMLInputElement) {
                        this.searchParams.append(includeElement.name, includeElement.value)
                    } else {
                        val namedElements = includeElement.querySelectorAll("[name]")

                        for (element in namedElements.asList()) {
                            if (element is HTMLInputElement) {
                                this.searchParams.append(element.name, element.value)
                            }
                        }
                    }
                }
            }

            valsQuery.forEach { (key, value) ->
                this.searchParams.append(key, value.jsonPrimitive.content)
            }
        }.toString()

        println("execute http $requestUrl ($method)")

        val requestHeaders = Headers()
        requestHeaders.append("Bliss-Request", "true")
        val elementIdOrNull = sourceElement?.id?.toString()?.ifEmpty { null }
        if (elementIdOrNull != null)
            requestHeaders.append("Bliss-Trigger-Element-Id", elementIdOrNull)

        val elementNameOrEmpty = sourceElement?.getAttribute("name")
        if (elementNameOrEmpty != null)
            requestHeaders.append("Bliss-Trigger-Element-Name", elementNameOrEmpty)

        for (header in headers) {
            requestHeaders.append(header.key, header.value)
        }

        val json = mutableMapOf<String, JsonElement>()

        // We do it like this instead of checking if json is empty because we WANT the body to be included even if it empty, as long as there was an attempt to include json or vals
        var includeBody = false

        if (includeJson != null) {
            includeBody = true
            val querySelectors = includeJson.split(",").map { it.trim() }

            for (selector in querySelectors) {
                val includeElements = document.querySelectorAll(selector).asList()

                json += createMapOfElementValues("name", includeElements, remapJsonKeys)
            }
        }

        if (valsJson.isNotEmpty()) {
            includeBody = true
            valsJson.forEach { (key, value) ->
                json[key] = value
            }
        }

        val detail = BlissProcessRequestJsonBody(sourceElement, json, includeBody)
        val event = CustomEvent(
            type = EventType("bliss:processRequestJsonBody"),
            init = CustomEventInit(
                detail = detail
            )
        )

        document.dispatchEvent(event)

        val bodyInit = if (detail.includeBody) {
            val bodyAsJson = Json.encodeToString(json)
            println("Including JSON on the request: $bodyAsJson")
            BodyInit(bodyAsJson)
        } else null


        val httpRequest = fetch(
            requestUrl,
            RequestInit(
                method = method.toRequestMethod(),
                headers = requestHeaders,
                body = bodyInit
            )
        )

        println("result: ${httpRequest.status}")

        val blissRedirectUrl = httpRequest.headers.get("Bliss-Redirect")
        if (blissRedirectUrl != null) {
            println("Redirecting to $blissRedirectUrl...")
            location.replace(blissRedirectUrl)
            return
        }

        val blissRefresh = httpRequest.headers.get("Bliss-Refresh")
        if (blissRefresh == "true") {
            println("Refreshing webpage...")
            location.reload()
            return
        }

        val body = httpRequest.text()

        val parser = DOMParser()
        val doc = parser.parseFromString(body, DOMParserSupportedType.textHtml)

        // We process all show-toast/show-modal/etc here, because sometimes the server does return one of the special attributes
        // and we want to process it, even if it is not included in the DOM
        val documentParsedEvent = CustomEvent(
            type = EventType("bliss:documentParsed"),
            init = CustomEventInit(
                detail = BlissDocumentParsed(doc),
                bubbles = true, // let it bubble up the DOM
                composed = true // cross-shadow DOM boundary if needed
            )
        )

        document.dispatchEvent(documentParsedEvent)

        val triggerEventHack = doc.querySelectorAll("[bliss-event]").asList()
        for (element in triggerEventHack) {
            val event = CustomEvent(
                type = EventType(element.getAttribute("bliss-event")!!),
                init = CustomEventInit(
                    detail = null,
                    bubbles = true, // let it bubble up the DOM
                    composed = true // cross-shadow DOM boundary if needed
                )
            )

            // This is a special attribute!
            element.remove()
            document.querySelectorAll(element.getAttribute("bliss-event-target")!!)
                .asList()
                .forEach {
                    it.dispatchEvent(event)
                }
        }

        val setAttributesHack = doc.querySelectorAll("[bliss-set-attributes]").asList()
        for (element in setAttributesHack) {
            // This is a special attribute!
            element.remove()

            val newAttributes = element.getAttribute("bliss-attributes")!!.let {
                Json.decodeFromString<Map<String, String>>(it)
            }

            for (targetElement in document.querySelectorAll(element.getAttribute("bliss-set-attributes")!!).asList()) {
                for ((key, value) in newAttributes) {
                    targetElement.setAttribute(key, value)
                }
            }
        }

        val pageTitle = doc.querySelector("title")

        var didSwap = false

        // If the server sent a reswap, we expect that it overrides ANYTHING and EVERYTHING, no matter the response code!
        var swapValue = httpRequest.headers.get("Bliss-Reswap")
        val statusCode = HttpStatusCode.fromValue(httpRequest.status.toInt())

        if (swapValue == null) {
            // We do this way because we can select the responses based on the status response, sweet!
            // The default will be "nothing", to avoid weird errors (like client or server errors) being swapped to the DOM
            val swapRequest = swaps.firstOrNull { statusCode in it.statusCodes }
            if (swapRequest != null) {
                swapValue = swapRequest.swapValue
            }
        }

        if (swapValue != null && sourceElement != null) {
            didSwap = doSwap(
                swapValue,
                sourceElement,
                sourceDocument = doc,
                targetDocument = document
            )
        }

        println("Did any swap happen on this request? $didSwap")
        if (didSwap) {
            println("Because a swap happened, we will process all attributes in the body...")
            // We process it after the fact, due to things like the "bliss-disabled-when" failing when trying to figure out which element it should match
            processAttributes(document.body)
        }

        // Just like reswaps, we expect that it overrides ANYTHING and EVERYTHING, no matter the response code!
        var pushUrlValue = httpRequest.headers.get("Bliss-Push-Url")

        if (pushUrlValue == null) {
            // We do this way because we can select the responses based on the status response, sweet!
            // The default will be "nothing", to avoid weird errors (like client or server errors) being pushed
            val pushRequest = pushUrls.firstOrNull { statusCode in it.statusCodes }
            if (pushRequest != null) {
                pushUrlValue = pushRequest.pushUrl
            }
        }

        println("pushUrlValue (raw): $pushUrlValue")

        if (pushUrlValue != null) {
            val realPushUrl = if (pushUrlValue == "true") {
                requestUrl
            } else {
                pushUrlValue
            }

            println("pushUrlValue (parsed): $realPushUrl")

            val state = unsafeJso<dynamic> {
                this.blessed = true
            }

            history.pushState(state, "", realPushUrl)
        }

        if (pageTitle != null) {
            // Replace title!
            document.querySelector("title")?.replaceWith(pageTitle.cloneNode(true))
        }
    }

    data class SwapRequest(
        val statusCodes: Set<HttpStatusCode>,
        val swapValue: String
    )

    data class PushUrlRequest(
        val statusCodes: Set<HttpStatusCode>,
        val pushUrl: String
    )

    class BlissRequestManager() {
        var inflight: Job? = null
    }
}