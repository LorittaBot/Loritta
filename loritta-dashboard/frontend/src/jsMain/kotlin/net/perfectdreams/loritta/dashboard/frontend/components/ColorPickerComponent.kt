package net.perfectdreams.loritta.dashboard.frontend.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.perfectdreams.bliss.BlissComponent
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.compose.components.colorpicker.Color
import net.perfectdreams.loritta.dashboard.frontend.compose.components.colorpicker.ColorPicker
import org.jetbrains.compose.web.renderComposable
import web.dom.document
import web.html.HTMLInputElement
import web.input.INPUT
import web.input.InputEvent

class ColorPickerComponent(val m: LorittaDashboardFrontend) : BlissComponent<HTMLInputElement>() {
    override fun onMount() {
        // We use a text input instead of a color input as the "backing" element because the color input doesn't have a concept of "color not set", because
        // it always falls back to a black color
        mountedElement.style.display = "none"

        val rootNode = document.createElement("div")
        mountedElement.before(rootNode)

        val initialValue = if (mountedElement.value.isNotBlank()) Color.fromHex(mountedElement.value) else null

        renderComposable(rootNode) {
            var currentColor by remember { mutableStateOf(initialValue) }

            // TODO: We need to somehow provide the SVG icons here
            ColorPicker(m, TODO(), TODO(), currentColor) {
                currentColor = it
                mountedElement.value = it?.toHex() ?: ""
                mountedElement.dispatchEvent(InputEvent(InputEvent.INPUT))
            }
        }
    }

    override fun onUnmount() {}
}