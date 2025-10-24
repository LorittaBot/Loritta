package net.perfectdreams.loritta.dashboard.frontend.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import js.array.asList
import kotlinx.serialization.json.Json
import net.perfectdreams.bliss.BlissComponent
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.dashboard.EmbeddedModal
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.compose.components.FancySelectMenu
import net.perfectdreams.loritta.dashboard.frontend.compose.components.FancySelectMenuEntry
import net.perfectdreams.loritta.dashboard.frontend.compose.components.RawHtml
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import web.dom.document
import web.events.CHANGE
import web.events.Event
import web.events.EventInit
import web.html.HTMLSelectElement
import web.input.INPUT
import web.input.InputEvent
import web.input.InputEventInit
import web.mutation.MutationObserver
import web.mutation.MutationObserverInit
import web.mutation.MutationRecordType
import web.mutation.attributes

class FancySelectMenuComponent(val m: LorittaDashboardFrontend) : BlissComponent<HTMLSelectElement>() {
    val observers = mutableListOf<MutationObserver>()

    override fun onMount() {
        this.mountedElement.style.display = "none"
        val chevronSVG = this.mountedElement.getAttribute("fancy-select-menu-chevron-svg")!!

        val rootNode = document.createElement("div")
        this.mountedElement.before(rootNode)

        var disabled by mutableStateOf(this.mountedElement.disabled)

        val observer = MutationObserver { mutationList, observer ->
            for (record in mutationList) {
                if (record.type == MutationRecordType.attributes && record.attributeName == "disabled") {
                    disabled = this.mountedElement.disabled
                }
            }
        }

        observer.observe(this.mountedElement, MutationObserverInit(attributes = true, attributeFilter = arrayOf("disabled"), attributeOldValue = true))
        this.observers.add(observer)

        renderComposable(rootNode) {
            fun buildEntriesFromState(): List<FancySelectMenuEntry> {
                return mountedElement.options
                    .asList()
                    .map {
                        FancySelectMenuEntry(
                            {
                                val htmlLabel = it.getAttribute("fancy-select-menu-label")
                                if (htmlLabel != null) {
                                    RawHtml(htmlLabel)
                                } else {
                                    Text(it.label)
                                }
                            },
                            value = it.value,
                            it.selected,
                            it.disabled,
                            it.getAttribute("fancy-select-menu-open-modal-if-disabled")
                                ?.let {
                                    Json.decodeFromString<EmbeddedModal>(BlissHex.decodeFromHexString(it))
                                }
                        )
                    }
            }

            var entries by remember { mutableStateOf(buildEntriesFromState()) }

            FancySelectMenu(
                m.modalManager,
                "test",
                maxValues = 1,
                chevronSVG,
                disabled,
                entries
            ) { values ->
                mountedElement.options.asList().forEach { it.selected = it.value in values }
                // Dispatch to trigger anyone that's listening to this

                // Yes, we need to dispatch BOTH because listeners may listen to the input OR the change event
                mountedElement.dispatchEvent(
                    InputEvent(
                        InputEvent.INPUT,
                        InputEventInit(bubbles = true)
                    )
                )
                mountedElement.dispatchEvent(
                    Event(
                        Event.CHANGE,
                        EventInit(bubbles = true)
                    )
                )

                entries = buildEntriesFromState()
            }
        }
    }

    override fun onUnmount() {
        this.observers.forEach {
            it.disconnect()
        }
        this.observers.clear()
    }
}