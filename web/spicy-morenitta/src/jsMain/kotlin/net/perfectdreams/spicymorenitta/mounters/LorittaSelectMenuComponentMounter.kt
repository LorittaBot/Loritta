package net.perfectdreams.spicymorenitta.mounters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import decodeURIComponent
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.components.HtmlText
import net.perfectdreams.spicymorenitta.components.SimpleSelectMenu
import net.perfectdreams.spicymorenitta.components.SimpleSelectMenuEntry
import net.perfectdreams.spicymorenitta.utils.Logging
import net.perfectdreams.spicymorenitta.utils.querySelectorAll
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import web.dom.document
import web.events.Event
import web.events.EventInit
import web.html.HTMLElement
import web.html.HTMLOptionElement
import web.html.HTMLSelectElement

class LorittaSelectMenuComponentMounter(val m: SpicyMorenitta) : SimpleComponentMounter("loritta-select-menu"), Logging {
    override fun simpleMount(element: HTMLElement) {
        val originalSelectMenuElement = element
        if (originalSelectMenuElement.getAttribute("loritta-powered-up") != null)
            return

        if (originalSelectMenuElement !is HTMLSelectElement)
            throw RuntimeException("Select Menu Component is not a HTMLSelectElement!")

        originalSelectMenuElement.setAttribute("loritta-powered-up", "")
        val originalStyle = originalSelectMenuElement.getAttribute("style")

        // Hide the original select menu
        originalSelectMenuElement.style.display = "none"

        // val selectMenuName = originalSelectMenuElement.getAttribute("name")
        // originalSelectMenuElement.removeAttribute("name")

        val htmlOptions = originalSelectMenuElement.querySelectorAll<HTMLOptionElement>("option")

        val originalEntries = htmlOptions.map {
            val openEmbeddedModalOnSelect = it.getAttribute("loritta-select-menu-open-embedded-modal-on-select")
            val embeddedSpicyModal = openEmbeddedModalOnSelect?.let { kotlinx.serialization.json.Json.decodeFromString<EmbeddedSpicyModal>(decodeURIComponent(it)) }

            SimpleSelectMenuEntry(
                {
                    val textHTML = it.getAttribute("loritta-select-menu-text")
                    if (textHTML != null) {
                        HtmlText(textHTML)
                    } else {
                        Text(it.innerHTML)
                    }
                },
                it.value,
                it.selected,
                // We don't use HTML "disabled" attribute because that doesn't let the entry be serialized to a form
                it.getAttribute("loritta-select-menu-disabled")?.toBoolean() == true,
                embeddedSpicyModal
            )
        }

        val selectMenuWrapperElement = document.createElement("div")
        if (originalStyle != null)
            selectMenuWrapperElement.setAttribute("style", originalStyle)

        originalSelectMenuElement.parentElement!!.insertBefore(selectMenuWrapperElement, originalSelectMenuElement)

        renderComposable(selectMenuWrapperElement.unsafeCast<web.html.HTMLElement>()) {
            var modifiedEntries by remember { mutableStateOf(originalEntries) }

            // For some reason we need to key it by the modifiedEntries list
            // Because if a entry uses custom HTML it borks and the list is never updated
            SimpleSelectMenu(
                "Click Here!",
                if (originalSelectMenuElement.multiple) null else 1,
                modifiedEntries
            ) {
                debug("owo!!!")
                debug("Selected $it")
                debug("Original: ${modifiedEntries.toList()}")
                val selectedEntries = originalEntries.filter { entry ->
                    entry.value in it
                }

                for (selectedEntry in selectedEntries) {
                    if (selectedEntry.disabled) {
                        // Selecting a disabled entry!
                        if (selectedEntry.embeddedSpicyModalToBeOpenedIfDisabled != null) {
                            // The entry has a embedded modal!
                            m.modalManager.openModal(selectedEntry.embeddedSpicyModalToBeOpenedIfDisabled)
                        }
                        return@SimpleSelectMenu
                    }
                }

                modifiedEntries = originalEntries.map { copiedEntry ->
                    copiedEntry.copy(selected = copiedEntry in selectedEntries)
                }
                debug("Modified: ${modifiedEntries.toList()}")

                // We are actually going to edit the original elements
                // originalSelectMenuElement.value = it.first()
                htmlOptions.forEach { htmlOption ->
                    htmlOption.selected = htmlOption.value in it
                }

                originalSelectMenuElement.dispatchEvent(Event(Event.INPUT, EventInit(bubbles = true)))

                originalSelectMenuElement.dispatchEvent(Event(Event.CHANGE, EventInit(bubbles = true)))
            }
        }
    }
}