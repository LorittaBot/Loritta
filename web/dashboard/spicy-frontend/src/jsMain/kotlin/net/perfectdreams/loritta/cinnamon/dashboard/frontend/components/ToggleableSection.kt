package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

/**
 * A block of toggleable sections
 */
@Composable
fun ToggleableSections(block: @Composable () -> (Unit)) {
    Div(attrs = {
        classes("toggleable-sections")
    }) {
        block.invoke()
    }
}

/**
 * A toggleable section controlled by a [DiscordToggle]
 */
@Composable
fun ToggleableSection(
    id: String,
    title: String,
    description: String?,
    checked: Boolean,
    onChange: (Boolean) -> (Unit),
    block: @Composable () -> (Unit)
) = ToggleableSection(id, { Text(title) }, description?.let { { Text(it) } }, checked, onChange, block)

/**
 * A toggleable section controlled by a [DiscordToggle]
 */
@Composable
fun ToggleableSection(
    id: String,
    title: String,
    description: String?,
    stateValue: MutableState<Boolean>,
    block: @Composable () -> (Unit)
) = ToggleableSection(id, { Text(title) }, description?.let { { Text(it) } }, stateValue.value, { stateValue.value = it }, block)

/**
 * A toggleable section controlled by a [DiscordToggle]
 */
@Composable
fun ToggleableSection(
    id: String,
    title: @Composable () -> (Unit),
    description: @Composable (() -> (Unit))? = null,
    checked: Boolean,
    onChange: (Boolean) -> (Unit),
    block: @Composable () -> (Unit)
) {
    Div(attrs = {
        classes("toggleable-section")
    }) {
        Div(attrs = {
            classes("toggleable-selection")
            if (checked)
                classes("is-open")
        }) {
            DiscordToggle(id, title, description, checked, onChange)
        }

        if (checked) {
            Div(attrs = {
                classes("toggleable-content")
            }) {
                block()
            }
        }
    }
}