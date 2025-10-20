package net.perfectdreams.loritta.dashboard.frontend.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text

/**
 * A toggle/switch that looks like Discord's toggles
 */
@Composable
fun DiscordToggle(
    id: String,
    title: String,
    description: String?,
    checked: Boolean,
    onChange: (Boolean) -> (Unit)
) = DiscordToggle(id, { Text(title) }, description?.let { { Text(it) } }, checked, onChange)

/**
 * A toggle/switch that looks like Discord's toggles
 */
@Composable
fun DiscordToggle(
    id: String,
    title: String,
    description: String?,
    stateValue: MutableState<Boolean>
) = DiscordToggle(id, { Text(title) }, description?.let { { Text(it) } }, stateValue.value, { stateValue.value = it })

/**
 * A toggle/switch that looks like Discord's toggles
 */
@Composable
fun DiscordToggle(
    id: String,
    title: @Composable () -> (Unit),
    description: @Composable (() -> (Unit))? = null,
    checked: Boolean,
    onChange: (Boolean) -> (Unit)
) = Label(forId = id, attrs = {
    classes("toggle-wrapper")
}) {
    Div(attrs = {
        classes("toggle-information")
    }) {
        Div(attrs = {
            classes("toggle-title")
        }) {
            title()
        }

        if (description != null) {
            Div(attrs = {
                classes("toggle-description")
            }) {
                description()
            }
        }
    }

    Div {
        Input(
            InputType.Checkbox,
            attrs = {
                id(id)

                onChange {
                    onChange.invoke(it.value)
                }

                checked(checked)
            }
        )

        Div(attrs = {
            classes("switch-slider", "round")
        })
    }
}