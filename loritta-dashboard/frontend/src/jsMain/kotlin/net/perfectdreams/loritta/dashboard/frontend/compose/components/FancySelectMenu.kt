package net.perfectdreams.loritta.dashboard.frontend.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import net.perfectdreams.loritta.dashboard.EmbeddedModal
import net.perfectdreams.loritta.dashboard.frontend.modals.ModalManager
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.events.Event

// ===[ CUSTOM SELECT MENU:TM: ]===
// Because styling a default select menu is hard as fuc
// Inspired by Discord's Select Menu
// Changed to fit spicy-morenitta's "component" behavior, this makes it more similar to how the DOM works and how Discord's select menu component behaves
// when programming a bot that uses it
// Also I tried "raw dogging html" to see if making this without Compose was going to be hard... and yes, it is VERY hard (not really hard but very cumbersome)
@Composable
fun FancySelectMenu(
    modalManager: ModalManager,
    placeholder: String,
    maxValues: Int? = 1,
    entries: List<FancySelectMenuEntry>,
    onSelect: (List<String>) -> (Unit)
) {
    val singleValueSelectMenu = maxValues == 1
    var isSelectMenuVisible by remember { mutableStateOf(false) }
    var clickEventListener by remember { mutableStateOf<((Event) -> Unit)?>(null) }
    var keydownEventListener by remember { mutableStateOf<((Event) -> Unit)?>(null) }

    // This is a hack to support changing elements via key up/key down
    // When we press the key, the recomp key is changed, causing a SelectMenu recomposition
    var recomp by remember { mutableStateOf(0) }

    console.log("SelectMenu recomposition hack counter: $recomp")
    println("Entries to be rendered: ${entries.map { it.value }}")
    println("Selected entries to be rendered: ${entries.filter { it.selected }.map { it.value }}")

    key(recomp) {
        Div(attrs = {
            classes("select-wrapper")
        }) {
            Div(
                attrs = {
                    classes("select")

                    // According to the docs, "classes" acumulate instead of replacing!
                    if (isSelectMenuVisible)
                        classes("open")

                    onClick {
                        isSelectMenuVisible = !isSelectMenuVisible
                    }
                }
            ) {
                Div {
                    Div(attrs = {
                        classes("currently-selected-option-content")
                    }) {
                        val currentlySelectedOption = entries.firstOrNull { it.selected }
                        // TODO: Better Max Values handling
                        if (currentlySelectedOption == null || maxValues != 1) {
                            Text(placeholder)
                        } else {
                            currentlySelectedOption.content.invoke()
                        }
                    }
                    Div(attrs = { classes("chevron") }) {
                        I(attrs = { classes("fa-solid", "fa-chevron-down") })
                        // UIIcon(SVGIconManager.chevronDown)
                    }
                }
            }

            if (isSelectMenuVisible) {
                Div(attrs = {
                    classes("menu")

                    ref {
                        val clickCallback: ((Event) -> Unit) = {
                            isSelectMenuVisible = false
                        }

                        val keydownCallback: ((Event) -> Unit) = {
                            // For now, only single values are supported
                            if (singleValueSelectMenu) {
                                it.stopPropagation()
                                it.preventDefault() // Prevent default to avoid the entire screen scrolling

                                console.log("keydown callback invoked")

                                val keyCode = it.asDynamic().key
                                val shift = if (keyCode == "ArrowDown") {
                                    +1
                                } else if (keyCode == "ArrowUp") {
                                    -1
                                } else 0

                                if (shift != 0) {
                                    val selectedEntryIndex = entries.indexOfFirst { it.selected }
                                    if (selectedEntryIndex == -1) {
                                        // If none are selected, select the first of the list
                                        val selected = entries.first()
                                        onSelect.invoke(listOf(selected.value))
                                        recomp++
                                    } else {
                                        val selected = entries[selectedEntryIndex]
                                        val nextSel = entries.getOrNull(selectedEntryIndex + shift)
                                        // Only invoke if there is a next entry in the select menu!
                                        if (nextSel != null) {
                                            console.log("selected index is $selectedEntryIndex")

                                            onSelect.invoke(listOf(nextSel.value))
                                            recomp++
                                        }
                                    }
                                }
                            }
                        }

                        document.addEventListener("click", clickCallback)
                        document.addEventListener("keydown", keydownCallback)

                        clickEventListener = clickCallback
                        keydownEventListener = keydownCallback

                        onDispose {
                            document.removeEventListener("click", clickEventListener)
                            document.removeEventListener("keydown", keydownEventListener)
                            clickEventListener = null
                        }
                    }
                }) {
                    entries.forEach { entry ->
                        Div(
                            attrs = {
                                ref {
                                    // Automatically scrolls to the selected entry
                                    // We don't use scrollIntoView because it is "bad", as in: If you attempt to use the "center" option, it scrolls the entire page, which is not what we want!
                                    if (singleValueSelectMenu && entry.selected) {
                                        val parent = it.parentElement
                                        if (parent != null) {
                                            // Pretty sure the parent element cannot be nullable, but oh well
                                            parent.scrollTop = it.offsetTop.toDouble()
                                        }
                                    }

                                    onDispose {}
                                }

                                onClick {
                                    val isAlreadySelected = entry.selected
                                    if (singleValueSelectMenu && isAlreadySelected) {
                                        // Do not propagate to our click event listener
                                        it.stopPropagation()

                                        // But do close the select menu anyway
                                        isSelectMenuVisible = false
                                        return@onClick
                                    }

                                    var shouldInvoke = false
                                    if (maxValues == null)
                                        shouldInvoke = true
                                    if (maxValues != null) {
                                        if (maxValues > entries.count { it.selected })
                                            shouldInvoke = true
                                        else
                                            shouldInvoke = entry.selected
                                    }
                                    if (singleValueSelectMenu)
                                        shouldInvoke = true

                                    if (shouldInvoke) {
                                        if (entry.disabled) {
                                            // Oops! This is disabled!!
                                            if (entry.embeddedModalToBeOpenedIfDisabled != null)
                                                modalManager.openModal(entry.embeddedModalToBeOpenedIfDisabled)
                                        } else {
                                            if (entry.selected) {
                                                if (singleValueSelectMenu) {
                                                    onSelect.invoke(listOf(entry.value)) // Just reselect
                                                } else {
                                                    // If this is already selected, we will get all the current entries that are selected EXCEPT this one
                                                    onSelect.invoke(
                                                        entries
                                                            .filter { it.selected && it != entry }
                                                            .map { it.value }
                                                    )
                                                }
                                            } else {
                                                if (singleValueSelectMenu) {
                                                    onSelect.invoke(listOf(entry.value))
                                                } else {
                                                    // If we are selecting a new entry, select all the selected entries PLUS this one
                                                    onSelect.invoke(
                                                        entries
                                                            .filter { it.selected }
                                                            .map { it.value }
                                                            .toMutableList()
                                                            .apply {
                                                                this.add(entry.value)
                                                            }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (singleValueSelectMenu) {
                                        // When you select something (ONLY IN A SINGLE MAX VALUE MENU), it should automatically close
                                        isSelectMenuVisible = false
                                    }

                                    // Do not propagate to our click event listener
                                    it.stopPropagation()
                                }

                                classes("select-menu-entry")

                                if (entry.selected)
                                    classes("selected")
                            }
                        ) {
                            entry.content.invoke()
                        }
                    }
                }
            }
        }
    }
}

data class FancySelectMenuEntry(
    val content: @Composable () -> (Unit),
    val value: String,
    val selected: Boolean,
    val disabled: Boolean,
    val embeddedModalToBeOpenedIfDisabled: EmbeddedModal?
)