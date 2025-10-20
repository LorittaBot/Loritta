package net.perfectdreams.loritta.dashboard.frontend.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.perfectdreams.bliss.BlissComponent
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.HiddenInput
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import web.dom.Element
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLDivElement
import web.html.HTMLInputElement
import web.input.INPUT
import web.input.InputEvent

class CharacterCounterComponent : BlissComponent<HTMLDivElement>() {
    var characterCount by mutableStateOf(0)

    override fun onMount() {
        val elementToBeListenedTo = document.body.querySelector(mountedElement.getAttribute("character-counter-listen")!!) ?: error("Could not find element to listen to!")
        require(elementToBeListenedTo is HTMLInputElement)

        registeredEvents += elementToBeListenedTo.addEventHandler(InputEvent.INPUT) {
            characterCount = elementToBeListenedTo.value.length
        }

        renderComposable(mountedElement) {
            Text("$characterCount/${elementToBeListenedTo.maxLength}")
        }
    }

    override fun onUnmount() {}
}