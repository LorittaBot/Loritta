package net.perfectdreams.loritta.dashboard.frontend.compose.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import kotlin.random.Random

/**
 * A fancy radio button component with custom styling
 *
 * @param name The name attribute for the radio group
 * @param value The value attribute for this radio button
 * @param checked Whether this radio button is currently selected
 * @param onChange Callback when the radio button is clicked
 * @param radioContent Content to display next to the radio button (typically radio-option-info div)
 */
@Composable
fun FancyRadioInput(
    name: String,
    value: String,
    checked: Boolean,
    onChange: (String) -> Unit,
    radioContent: @Composable () -> Unit
) {
    val radioId = "radio-${Random.nextInt()}"

    Div(attrs = {
        classes("fancy-radio-option-wrapper")
    }) {
        Input(InputType.Radio, attrs = {
            id(radioId)
            attr("style", "display: none;")
            attr("name", name)
            attr("value", value)
            checked(checked)
            onChange { onChange(value) }
        })

        Label(forId = radioId, attrs = {
            classes("fancy-radio-option")
        }) {
            Div(attrs = {
                classes("fancy-radio-option-circle")
            }) {
                Div(attrs = {
                    classes("fancy-radio-option-circle-white")
                })
            }

            radioContent()
        }
    }
}
