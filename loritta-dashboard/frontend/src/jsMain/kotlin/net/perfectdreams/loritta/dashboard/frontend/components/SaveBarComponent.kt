package net.perfectdreams.loritta.dashboard.frontend.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import net.perfectdreams.bliss.Bliss
import net.perfectdreams.bliss.BlissComponent
import web.cssom.ClassName
import web.dom.Element
import web.dom.document
import web.events.*
import web.html.HTMLDivElement
import web.input.INPUT
import web.input.InputEvent
import web.window.window

class SaveBarComponent : BlissComponent<HTMLDivElement>() {
    companion object {
        // This is STUPIDLY hacky
        var saveBarActive by mutableStateOf<Boolean>(false)
    }

    lateinit var trackedSection: Element
    lateinit var originalState: JsonElement
    var alwaysDirty = false

    override fun onMount() {
        this.alwaysDirty = mountedElement.getAttribute("save-bar-always-dirty") == "true"
        setSaveBarState(this.alwaysDirty)

        val trackedSectionQuerySelector = mountedElement.getAttribute("save-bar-track-section") ?: error("I don't know which section to track!")
        val trackedSection = document.querySelector(trackedSectionQuerySelector)!!
        this.trackedSection = trackedSection

        fun updateSaveBarWidth() {
            println("Updating save bar width...")
            val width = document.querySelector("#right-sidebar-content-and-save-bar-wrapper")!!.getBoundingClientRect().width
            mountedElement.style.width = "${width}px"
        }

        registeredEvents.add(
            window.addEventHandler(Event.RESIZE) {
                updateSaveBarWidth()
            }
        )

        registeredEvents.add(
            window.addEventHandler(Event.SCROLL) {
                updateSaveBarWidth()
            }
        )

        scope.launch {
            // Does it look weird like this? Yeah, but we want the style to be recalculated on swap before we calculate the width
            updateSaveBarWidth()

            this@SaveBarComponent.originalState = Json.encodeToJsonElement(Bliss.createMapOfElementValues("loritta-config", listOf(trackedSection), mapOf()))

            registeredEvents += trackedSection.addEventHandler(InputEvent.INPUT) {
                if (!alwaysDirty) {
                    println("Element ${it.target} changed!")

                    GlobalScope.launch {
                        val state = Bliss.createMapOfElementValues("loritta-config", listOf(trackedSection), mapOf())
                        val stateAsJson = Json.encodeToJsonElement(state)
                        println("ORIGINAL STATE: $originalState")
                        println("STATE AS JSON: $stateAsJson")

                        if (stateAsJson != originalState) {
                            println("NEW STATE!")
                            mountedElement.classList.remove(ClassName("initial-state"))
                            mountedElement.classList.add(ClassName("has-changes"))
                            mountedElement.classList.remove(ClassName("no-changes"))
                            setSaveBarState(true)
                        } else {
                            println("OLD STATE!")
                            mountedElement.classList.add(ClassName("no-changes"))
                            mountedElement.classList.remove(ClassName("has-changes"))
                            setSaveBarState(false)
                        }
                    }
                }
            }

            registeredEvents += mountedElement.addEventHandler(EventType<Event>("resyncState")) { _ ->
                // When the configuration is saved, we will resync the state based on the current configuration!
                GlobalScope.launch {
                    originalState = Json.encodeToJsonElement(Bliss.createMapOfElementValues("loritta-config", listOf(trackedSection), mapOf()))

                    mountedElement.classList.add(ClassName("no-changes"))
                    mountedElement.classList.remove(ClassName("has-changes"))
                    setSaveBarState(false)
                    alwaysDirty = false
                }
            }
        }
    }

    override fun onUnmount() {
        setSaveBarState(false)
    }

    override fun onElementSwap(element: Element) {
        println("A element was swapped! $element")

        if (!alwaysDirty) {
            GlobalScope.launch {
                val state = Bliss.createMapOfElementValues("loritta-config", listOf(trackedSection), mapOf())
                val stateAsJson = Json.encodeToJsonElement(state)
                println("ORIGINAL STATE: $originalState")
                println("STATE AS JSON: $stateAsJson")

                if (stateAsJson != originalState) {
                    println("NEW STATE!")
                    mountedElement.classList.remove(ClassName("initial-state"))
                    mountedElement.classList.add(ClassName("has-changes"))
                    mountedElement.classList.remove(ClassName("no-changes"))
                    setSaveBarState(true)
                } else {
                    println("OLD STATE!")
                    mountedElement.classList.add(ClassName("no-changes"))
                    mountedElement.classList.remove(ClassName("has-changes"))
                    setSaveBarState(false)
                }
            }
        }
    }

    fun setSaveBarState(active: Boolean) {
        saveBarActive = active
        val detail = SaveBarState(active)

        val event = CustomEvent(
            type = EventType("loritta:saveBarState"),
            init = CustomEventInit(detail = detail)
        )

        document.dispatchEvent(event)
    }
}