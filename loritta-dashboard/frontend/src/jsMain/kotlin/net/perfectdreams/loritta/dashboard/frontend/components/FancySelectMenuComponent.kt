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
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import web.dom.document
import web.html.HTMLSelectElement
import web.input.INPUT
import web.input.InputEvent
import web.input.InputEventInit

class FancySelectMenuComponent(val m: LorittaDashboardFrontend) : BlissComponent<HTMLSelectElement>() {
    override fun onMount() {
        this.mountedElement.style.display = "none"

        val rootNode = document.createElement("div")
        this.mountedElement.before(rootNode)

        renderComposable(rootNode) {
            fun buildEntriesFromState(): List<FancySelectMenuEntry> {
                return mountedElement.options
                    .asList()
                    .map {
                        FancySelectMenuEntry(
                            { Text(it.label) },
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
                entries
            ) { values ->
                mountedElement.options.asList().forEach { it.selected = it.value in values }
                // Dispatch to trigger anyone that's listening to this
                mountedElement.dispatchEvent(
                    InputEvent(
                        InputEvent.INPUT,
                        InputEventInit(bubbles = true)
                    )
                )
                entries = buildEntriesFromState()
            }
        }
    }

    override fun onUnmount() {}
}