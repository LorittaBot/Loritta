@file:OptIn(ExperimentalEncodingApi::class)

package net.perfectdreams.bliss

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.utils.io.charsets.Charsets
import js.array.asList
import js.objects.unsafeJso
import js.typedarrays.toByteArray
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
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.dashboard.EmbeddedModal
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import web.blob.bytes
import web.cssom.ClassName
import web.dom.Document
import web.dom.Element
import web.dom.document
import web.events.CHANGE
import web.events.CustomEvent
import web.events.CustomEventInit
import web.events.Event
import web.events.EventHandler
import web.events.EventType
import web.events.addEventHandler
import web.history.POP_STATE
import web.history.PopStateEvent
import web.history.history
import web.html.HTMLInputElement
import web.html.HTMLSelectElement
import web.html.HTMLTextAreaElement
import web.html.InputType
import web.html.checkbox
import web.html.file
import web.html.radio
import web.input.INPUT
import web.input.InputEvent
import web.location.location
import web.navigator.navigator
import web.parsing.DOMParser
import web.parsing.DOMParserSupportedType
import web.parsing.textHtml
import web.pointer.CLICK
import web.pointer.PointerEvent
import web.sse.EventSource
import web.window.window
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// I find I'm here this place of bliss
object Bliss {
    private val SWAP_REGEX = Regex("(?<sourceQuerySelector>[A-Za-z#.-]+)( \\((?<sourceSwapType>[A-Za-z]+)\\))? -> (?<targetQuerySelector>[A-Za-z#.-]+)( \\((?<targetSwapType>[A-Za-z]+)\\))?")
    private val DISABLE_WHEN_REGEX = Regex("(?<querySelector>.+) (?<op>==|!=) (?<part>\"(?<text>.+)\"|(?<blank>blank)|(?<empty>empty))")

    private val http = HttpClient {
        expectSuccess = false
    }

    private val methods = setOf(
        HttpMethod.Get,
        HttpMethod.Post,
        HttpMethod.Put,
        HttpMethod.Patch,
        HttpMethod.Delete
    )

    private val componentBuilders = mutableMapOf<String, () -> (BlissComponent<*>)>()

    fun setupEvents() {
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

            processShowToastOnClick(element)
            processOpenModalOnClick(element)
            processCloseModalOnClick(element)
            processCopyTextOnClick(element)
            processBlissComponents(element)

            for (method in methods) {
                val requestMethodUrl = element.getAttribute("bliss-${method.value.lowercase()}") ?: continue

                val triggers = element.getAttribute("bliss-trigger")?.split(",")?.map { it.trim() } ?: listOf("click")
                val sync = element.getAttribute("bliss-sync")
                val includeQuery = element.getAttribute("bliss-include-query")
                val includeJson = element.getAttribute("bliss-include-json")
                val valsQuery = element.getAttribute("bliss-vals-query")
                val valsJson = element.getAttribute("bliss-vals-json")
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
                        swaps,
                        pushUrls
                    )
                }

                fun prepareAndExecuteHttp(disableElement: Boolean) {
                    val detail = BlissBeforeBlissRequestPrepare(element)

                    val event = CustomEvent(
                        type = EventType("bliss:beforeBlissRequestPrepare"),
                        init = CustomEventInit(detail = detail, cancelable = true)
                    )

                    document.dispatchEvent(event)

                    println("Default prevented? ${event.defaultPrevented}")
                    if (event.defaultPrevented)
                        return

                    if (disableElement) {
                        element.setAttribute("disabled", "")
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
                        element.removeAttribute("disabled")
                    }
                }

                for (triggerName in triggers) {
                    when (triggerName) {
                        "click" -> {
                            println("Setting up click event...")
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

                        is SSEBlissShowToast -> {
                            LorittaDashboardFrontend.INSTANCE.toastManager.showToast(sseEvent.toast)
                        }

                        is SSECustomEvent -> {
                            val query = document.querySelectorAll(sseEvent.eventTarget).asList()

                            if (query.isNotEmpty()) {
                                val event = CustomEvent(
                                    type = EventType(sseEvent.event),
                                    init = CustomEventInit(
                                        detail = null,
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

            val disableWhen = element.getAttribute("bliss-disable-when")
                ?.split(",")

            if (disableWhen != null) {
                println("BLISS DISABLE WHEN!!! $disableWhen")
                for (entry in disableWhen) {
                    val match = DISABLE_WHEN_REGEX.matchEntire(entry) ?: error("Failed to match $entry!")

                    val selector = match.groups["querySelector"]!!.value
                    val text = match.groups["text"]?.value
                    val blank = match.groups["blank"]?.value
                    val empty = match.groups["empty"]?.value
                    val op = match.groups["op"]?.value
                    val expectedBoolValue = op == "=="

                    println("Registering $selector for disabled when (text: $text, blank: $blank, empty: $empty)")
                    val targetEventElement = document.querySelector(selector) ?: error("Could not find element $selector!")

                    fun disableCondition(invokeBlock: () -> (Boolean)) {
                        val result = invokeBlock.invoke()

                        if (result) {
                            element.setAttribute("disabled", "")
                        } else {
                            element.removeAttribute("disabled")
                        }
                    }

                    if (targetEventElement is HTMLInputElement) {
                        fun blankCondition(bool: Boolean) {
                            if (targetEventElement.value.isBlank() == bool) {
                                element.removeAttribute("disabled")
                            } else {
                                element.setAttribute("disabled", "")
                            }
                        }

                        fun contentCondition(expected: String, bool: Boolean) {
                            val checkValue = targetEventElement.value == expected

                            if (checkValue == bool) {
                                element.removeAttribute("disabled")
                            } else {
                                element.setAttribute("disabled", "")
                            }
                        }

                        if (empty != null) {
                            val emptyCondition = {
                                targetEventElement.value.isEmpty() == expectedBoolValue
                            }

                            targetEventElement.addEventHandler(InputEvent.INPUT) {
                                disableCondition(emptyCondition)
                            }

                            disableCondition(emptyCondition)
                        } else if (blank != null) {
                            val blankCondition = {
                                targetEventElement.value.isBlank() == expectedBoolValue
                            }

                            targetEventElement.addEventHandler(InputEvent.INPUT) {
                                disableCondition(blankCondition)
                            }

                            disableCondition(blankCondition)
                        } else if (text != null) {
                            val textCondition = {
                                (targetEventElement.value == text) == expectedBoolValue
                            }

                            targetEventElement.addEventHandler(InputEvent.INPUT) {
                                disableCondition(textCondition)
                            }

                            disableCondition(textCondition)
                        }
                    } else if (targetEventElement is HTMLSelectElement) {
                        if (text != null) {
                            val textCondition = {
                                (targetEventElement.value == text) == expectedBoolValue
                            }

                            targetEventElement.addEventHandler(Event.CHANGE) {
                                disableCondition(textCondition)
                            }

                            disableCondition(textCondition)
                        }
                    }
                }
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

    fun doSwap(
        swapValue: String,
        element: Element,
        sourceDocument: Document,
        targetDocument: Document
    ): Boolean {
        // And finally... let's parse and execute the swap!
        val swaps = swapValue.split(",").map { it.trim() }

        var didSwap = false

        for (entry in swaps) {
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

                val sourceElementsCopy = sourceElements.toMutableList()
                println("Elements to be swapped #1: $sourceElementsCopy")

                // We need to use child nodes here, to avoid responses that only have text nodes being "ignored"
                when (targetSwapType) {
                    "outerHTML" -> targetElement.replaceWith(*sourceElements.toTypedArray())
                    "innerHTML" -> targetElement.replaceChildren(*sourceElements.toTypedArray())
                    else -> error("Unknown swap type: $targetSwapType")
                }

                println("Elements to be swapped #2: $sourceElementsCopy")
                val componentsInTheDOM = document.querySelectorAll("[bliss-component]").asList()
                for (component in componentsInTheDOM) {
                    val blissComponent = component.asDynamic().blissComponent
                    println("BlissComponent: $blissComponent (${blissComponent is BlissComponent<*>})")
                    console.log(blissComponent)

                    if (blissComponent != null) {
                        blissComponent as BlissComponent<*>

                        for (sourceElement in sourceElementsCopy) {
                            if (sourceElement is Element) {
                                println("Triggering onElementSwap for $blissComponent!")
                                blissComponent.onElementSwap(sourceElement)
                            } else {
                                println("Ignoring $sourceElement because it isn't an element")
                            }
                        }
                    }
                }

                didSwap = true
            }
        }

        return didSwap
    }

    private fun processShowToastOnClick(element: Element) {
        val toastOnClick = element.getAttribute("bliss-show-toast-on-click")

        if (toastOnClick != null) {
            val content = element.getAttribute("bliss-toast") ?: error("Missing bliss-toast attribute on a bliss-show-toast-on-click!")
            val toast = Json.decodeFromString<EmbeddedToast>(BlissHex.decodeFromHexString(content))

            element.addEventHandler(PointerEvent.CLICK) {
                LorittaDashboardFrontend.INSTANCE.toastManager.showToast(toast)
            }
        }
    }

    private fun processOpenModalOnClick(element: Element) {
        val modalOnClick = element.getAttribute("bliss-open-modal-on-click")

        if (modalOnClick != null) {
            val content = element.getAttribute("bliss-modal") ?: error("Missing bliss-modal attribute on a bliss-open-modal-on-click!")
            val modal = Json.decodeFromString<EmbeddedModal>(BlissHex.decodeFromHexString(content))

            element.addEventHandler(PointerEvent.CLICK) {
                LorittaDashboardFrontend.INSTANCE.modalManager.openModal(modal)
            }
        }
    }

    private fun processCloseModalOnClick(element: Element) {
        val closeModalOnClick = element.getAttribute("bliss-close-modal-on-click")

        if (closeModalOnClick != null) {
            element.addEventHandler(PointerEvent.CLICK) {
                LorittaDashboardFrontend.INSTANCE.modalManager.closeModal()
            }
        }
    }

    private fun processCopyTextOnClick(element: Element) {
        val copyTextOnClick = element.getAttribute("bliss-copy-text-on-click")

        if (copyTextOnClick != null) {
            element.addEventHandler(PointerEvent.CLICK) {
                navigator.clipboard.writeTextAsync(copyTextOnClick)
            }
        }
    }

    private fun processBlissComponents(element: Element) {
        val componentId = element.getAttribute("bliss-component")
        if (componentId != null) {
            try {
                val componentConstructor = componentBuilders[componentId] ?: error("Could not find component $componentId!")
                val component = componentConstructor.invoke()
                element.asDynamic().blissComponent = component
                component.mount(element)

                element.whenRemovedFromDOM {
                    component.unmount()
                }
            } catch (e: Throwable) {
                println("Something went wrong while trying to setup component \"$componentId\"! Skipping...")
                e.printStackTrace()
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
        includeElements: List<Element>
    ): MutableMap<String, JsonElement> {
        val json = mutableMapOf<String, JsonElement>()

        for (includeElement in includeElements) {
            fun setOrCreateList(key: String, newValue: JsonElement) {
                val isArray = key.endsWith("[]")
                if (isArray) {
                    val nameWithoutBrackets = key.removeSuffix("[]")

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
                    json[key] = newValue
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
                            val fileAsBytes = file.bytes().toByteArray()
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
        swaps: List<SwapRequest>,
        pushUrls: List<PushUrlRequest>,
    ) {
        val requestUrl = URLBuilder(baseUrl).apply {
            if (includeQuery != null) {
                val querySelectors = includeQuery.split(",").map { it.trim() }

                for (selector in querySelectors) {
                    val includeElement = document.querySelector(selector) ?: error("Could not find element $selector!")

                    if (includeElement is HTMLInputElement) {
                        this.parameters.append(includeElement.name, includeElement.value)
                    } else {
                        val namedElements = includeElement.querySelectorAll("[name]")

                        for (element in namedElements.asList()) {
                            if (element is HTMLInputElement) {
                                this.parameters.append(element.name, element.value)
                            }
                        }
                    }
                }
            }

            valsQuery.forEach { (key, value) ->
                this.parameters.append(key, value.jsonPrimitive.content)
            }
        }.buildString()

        println("execute http $requestUrl ($method)")

        val httpRequest = http.request(requestUrl) {
            this.method = method

            header("Bliss-Request", "true")

            val elementIdOrNull = sourceElement?.id?.toString()?.ifEmpty { null }
            if (elementIdOrNull != null)
                header("Bliss-Trigger-Element-Id", elementIdOrNull)

            val elementNameOrEmpty = sourceElement?.getAttribute("name")
            if (elementNameOrEmpty != null)
                header("Bliss-Trigger-Element-Name", elementNameOrEmpty)

            for (header in headers) {
                header(header.key, header.value)
            }

            val json = mutableMapOf<String, JsonElement>()

            // We do it like this instead of checking if json is empty because we WANT the body to be included even if it empty, as long as there was an attempt to include json or vals
            var includeBody = false

            if (includeJson != null) {
                includeBody = true
                val querySelectors = includeJson.split(",").map { it.trim() }

                for (selector in querySelectors) {
                    val includeElements = document.querySelectorAll(selector).asList()

                    json += createMapOfElementValues("name", includeElements)
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

            if (detail.includeBody) {
                val bodyAsJson = Json.encodeToString(json)
                println("Including JSON on the request: $bodyAsJson")
                setBody(TextContent(bodyAsJson, ContentType.Application.Json))
            }
        }
        println("result: ${httpRequest.status}")

        val blissRedirectUrl = httpRequest.headers["Bliss-Redirect"]
        if (blissRedirectUrl != null) {
            println("Redirecting to $blissRedirectUrl...")
            location.replace(blissRedirectUrl)
            return
        }

        val blissRefresh = httpRequest.headers["Bliss-Refresh"]
        if (blissRefresh == "true") {
            println("Refreshing webpage...")
            location.reload()
            return
        }

        val body = httpRequest.bodyAsText(Charsets.UTF_8)

        val parser = DOMParser()
        val doc = parser.parseFromString(body, DOMParserSupportedType.textHtml)

        // We process all show-toast/show-modal/etc here, because sometimes the server does return one of the special attributes
        // and we want to process it, even if it is not included in the DOM
        val showToastHack = doc.querySelectorAll("[bliss-show-toast]")
        for (element in showToastHack.asList()) {
            // This is a special attribute!
            val content = element.getAttribute("bliss-toast") ?: error("Missing bliss-toast attribute on a bliss-show-toast!")
            val toast = Json.decodeFromString<EmbeddedToast>(BlissHex.decodeFromHexString(content))
            element.remove()

            LorittaDashboardFrontend.INSTANCE.toastManager.showToast(toast)
        }

        val showModalHack = doc.querySelectorAll("[bliss-show-modal]")
        for (element in showModalHack.asList()) {
            // This is a special attribute!
            val content = element.getAttribute("bliss-modal") ?: error("Missing bliss-toast attribute on a bliss-modal!")
            val modal = Json.decodeFromString<EmbeddedModal>(BlissHex.decodeFromHexString(content))
            element.remove()

            LorittaDashboardFrontend.INSTANCE.modalManager.openModal(modal)
        }

        val closeModalHack = doc.querySelectorAll("[bliss-close-modal]")
        for (element in closeModalHack.asList()) {
            // This is a special attribute!
            element.remove()
            LorittaDashboardFrontend.INSTANCE.modalManager.closeModal()
        }

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

        val playSoundEffectHack = doc.querySelectorAll("[bliss-sound-effect]").asList()
        for (element in playSoundEffectHack) {
            val sfx = element.getAttribute("bliss-sound-effect")!!
            when (sfx) {
                "configSaved" -> LorittaDashboardFrontend.INSTANCE.soundEffects.configSaved.play(1.0)
                else -> error("Unknown SFX \"$sfx\"!")
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
        var swapValue = httpRequest.headers["Bliss-Reswap"]

        if (swapValue == null) {
            // We do this way because we can select the responses based on the status response, sweet!
            // The default will be "nothing", to avoid weird errors (like client or server errors) being swapped to the DOM
            val swapRequest = swaps.firstOrNull { httpRequest.status in it.statusCodes }
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
        var pushUrlValue = httpRequest.headers["Bliss-Push-Url"]

        println("pushUrlValue: $pushUrlValue")

        if (pushUrlValue == null) {
            // We do this way because we can select the responses based on the status response, sweet!
            // The default will be "nothing", to avoid weird errors (like client or server errors) being pushed
            val pushRequest = pushUrls.firstOrNull { httpRequest.status in it.statusCodes }
            if (pushRequest != null) {
                pushUrlValue = pushRequest.pushUrl
            }
        }

        if (pushUrlValue != null) {
            val realPushUrl = if (pushUrlValue == "true") {
                requestUrl
            } else {
                pushUrlValue
            }

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