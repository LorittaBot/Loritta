package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import kotlinx.html.DIV
import kotlinx.html.InputType
import kotlinx.html.div
import kotlinx.html.input
import kotlinx.html.js.onChangeFunction
import kotlinx.html.label
import org.w3c.dom.HTMLInputElement

fun DIV.createToggle(title: String, subText: String? = null, id: String? = null, isChecked: Boolean = false, onChange: ((Boolean) -> (Boolean))? = null) {
    div(classes = "toggleable-wrapper") {
        div(classes = "information") {
            div {
                + title
            }
            if (subText != null) {
                div(classes = "sub-text") {
                    + subText
                }
            }
        }

        label("switch") {
            attributes["for"] = id ?: "checkbox"

            input(InputType.checkBox) {
                attributes["id"] = id ?: "checkbox"

                if (isChecked) {
                    attributes["checked"] = "true"
                }

                onChangeFunction = {
                    val target = it.target as HTMLInputElement

                    val result = onChange?.invoke(target.checked) ?: target.checked

                    target.checked = result
                }
            }
            div(classes = "slider round") {}
        }
    }
}