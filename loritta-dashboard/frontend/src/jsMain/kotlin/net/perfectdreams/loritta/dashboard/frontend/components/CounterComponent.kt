package net.perfectdreams.loritta.dashboard.frontend.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.perfectdreams.luna.bliss.BlissComponent
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.HiddenInput
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import web.html.HTMLDivElement

/**
 * A sample counter component
 */
class CounterComponent : BlissComponent<HTMLDivElement>() {
    var value by mutableStateOf(0)

    override fun onMount() {
        value = mountedElement.getAttribute("counter-value")?.toInt() ?: 0

        renderComposable(mountedElement) {
            Button(attrs = {
                onClick {
                    value++
                }
            }) {
                Text("Click Me! ($value)")
            }

            HiddenInput(attrs = {
                name("counterValue")
                value(value)
            })
        }
    }

    override fun onUnmount() {
        println("I was removed from the DOM :(")
    }
}