package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.*
import kotlinx.browser.document
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.events.Event

// ===[ CUSTOM SELECT MENU:TM: ]===
// Because styling a default select menu is hard as fuc
// Inspired by Discord's Select Menu
@Composable
fun SelectMenu(
    placeholder: String,
    maxValues: Int? = 1,
    entries: List<SelectMenuEntry>,
) {
    val singleValueSelectMenu = maxValues == 1
    var isSelectMenuVisible by remember { mutableStateOf(false) }
    var clickEventListener by remember { mutableStateOf<((Event) -> Unit)?>(null) }
    var keydownEventListener by remember { mutableStateOf<((Event) -> Unit)?>(null) }

    // This is a hack to support changing elements via key up/key down
    // When we press the key, the recomp key is changed, causing a SelectMenu recomposition
    var recomp by remember { mutableStateOf(0) }

    console.log("SelectMenu recomposition hack counter: $recomp")

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
                        UIIcon(SVGIconManager.chevronDown)
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

                                console.log("keydown callback invoked")

                                val keyCode = it.asDynamic().keyCode
                                val shift = if (keyCode == 40) {
                                    +1
                                } else {
                                    -1
                                }

                                val selectedEntryIndex = entries.indexOfFirst { it.selected }
                                if (selectedEntryIndex == -1) {
                                    // If none are selected, select the first of the list
                                    val selected = entries.first()
                                    selected.onSelect.invoke()
                                    recomp++
                                } else {
                                    val selected = entries[selectedEntryIndex]
                                    val nextSel = entries.getOrNull(selectedEntryIndex + shift)
                                    // Only invoke if there is a next entry in the select menu!
                                    if (nextSel != null) {
                                        console.log("selected index is $selectedEntryIndex")

                                        selected.onDeselect.invoke()
                                        nextSel.onSelect.invoke()

                                        recomp++
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

                                    val shouldInvoke =
                                        maxValues == null || singleValueSelectMenu || (maxValues > entries.count { it.selected })

                                    if (shouldInvoke) {
                                        if (entry.selected) {
                                            entry.onDeselect.invoke()
                                        } else {
                                            entry.onSelect.invoke()
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

data class SelectMenuEntry(
    val content: @Composable () -> (Unit),
    val selected: Boolean,
    val onSelect: () -> (Unit),
    val onDeselect: () -> (Unit)
)