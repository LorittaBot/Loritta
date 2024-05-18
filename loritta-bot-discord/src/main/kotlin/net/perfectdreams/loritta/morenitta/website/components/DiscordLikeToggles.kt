package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.*
import java.util.*

object DiscordLikeToggles {
    fun FlowContent.discordToggle(
        checkboxName: String,
        title: String,
        description: String?,
        checked: Boolean,
        inputBehavior: INPUT.() -> (Unit)
    ) {
        discordToggle(
            "${UUID.randomUUID()}-toggle",
            checkboxName,
            title,
            description,
            checked,
            inputBehavior
        )
    }

    fun FlowContent.discordToggle(
        checkboxId: String,
        checkboxName: String,
        title: String,
        description: String?,
        checked: Boolean,
        inputBehavior: INPUT.() -> (Unit)
    ) {
        label(classes = "toggle-wrapper") {
            htmlFor = checkboxId
            div(classes = "toggle-information") {
                div(classes = "toggle-title") {
                    text(title)
                }

                if (description != null) {
                    div(classes = "toggle-description") {
                        text(description)
                    }
                }
            }
            div {
                input {
                    id = checkboxId
                    name = checkboxName
                    type = InputType.checkBox
                    this.checked = checked
                    inputBehavior.invoke(this)
                }

                div(classes = "switch-slider round") {}
            }
        }
    }

    fun FlowContent.toggleableSection(
        checkboxName: String,
        title: String,
        description: String?,
        checked: Boolean,
        content: DIV.() -> (Unit)
    ) {
        toggleableSection(
            "${UUID.randomUUID()}-toggle",
            checkboxName,
            title,
            description,
            checked,
            content
        )
    }

    fun FlowContent.toggleableSection(
        checkboxId: String,
        checkboxName: String,
        title: String,
        description: String?,
        checked: Boolean,
        content: DIV.() -> (Unit)
    ) {
        div(classes = "toggleable-section") {
            if (checked)
                classes += "is-open"

            div(classes = "toggleable-selection") {
                discordToggle(
                    checkboxId,
                    checkboxName,
                    title,
                    description,
                    checked
                ) {
                    // TODO - htmx-adventures: Cancel toggle until the page is fully loaded? (after hyperscript is loaded)
                    //  or maybe use a init block
                    attributes["_"] = """
                                init
                                    if me.checked
                                        add .is-open to the closest .toggleable-section
                                    else
                                        remove .is-open from the closest .toggleable-section
                                    end
                                end
                                
                                on change
                                    if me.checked
                                        add .is-open to the closest .toggleable-section
                                    else
                                        remove .is-open from the closest .toggleable-section
                                    end
                                end
                                """.trimIndent()
                }
            }

            div(classes = "toggleable-content") {
                content.invoke(this)
            }
        }
    }
}