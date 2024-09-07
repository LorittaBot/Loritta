package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.*
import net.perfectdreams.loritta.morenitta.website.utils.tsukiScript
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
        discordToggle(
            checkboxId,
            checkboxName,
            {
                text(title)
            },
            if (description != null) {
                {
                    text(description)
                }
            } else null,
            checked,
            inputBehavior
        )
    }

    fun FlowContent.discordToggle(
        checkboxId: String,
        checkboxName: String,
        title: (DIV.() -> (Unit)),
        description: (DIV.() -> (Unit))? = null,
        checked: Boolean,
        inputBehavior: INPUT.() -> (Unit)
    ) {
        label(classes = "toggle-wrapper") {
            htmlFor = checkboxId
            div(classes = "toggle-information") {
                div(classes = "toggle-title") {
                    title()
                }

                if (description != null) {
                    div(classes = "toggle-description") {
                        description()
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
        toggleableSection(
            checkboxId,
            checkboxName,
            {
                text(title)
            },
            if (description != null) {
                {
                    text(description)
                }
            } else null,
            checked,
            content
        )
    }

    fun FlowContent.toggleableSection(
        checkboxId: String,
        checkboxName: String,
        title: (DIV.() -> (Unit)),
        description: (DIV.() -> (Unit))? = null,
        checked: Boolean,
        content: (DIV.() -> (Unit))? = null
    ) {
        div(classes = "toggleable-section") {
            if (checked)
                classes += "is-open"
            if (content != null)
                classes += "section-content-not-empty"

            div(classes = "toggleable-selection") {
                discordToggle(
                    checkboxId,
                    checkboxName,
                    title,
                    description,
                    checked
                ) {
                }

                // In this case, "self" does not mean "the input" because the script is added AFTER the input because a script tag cannot be inside an input
                // (This is intentional, a script CANNOT BE inside a input!!!)
                // And in that case, "self" is the parent div
                // To work around this, we'll get the first input inside self
                // language=JavaScript
                tsukiScript(code = """
                        const input = self.selectFirst("input")
                        const toggleableSelection = input.closest(".toggleable-section");
                        function setClassOnToggleableSelection() {
                            console.log(input)
                            console.log(input.checked)
                            if (input.checked)
                                toggleableSelection.addClass("is-open")
                            else
                                toggleableSelection.removeClass("is-open")
                        }
                        
                        setClassOnToggleableSelection()

                        input.on("change", (e) => {
                            setClassOnToggleableSelection()
                        })
                    """.trimIndent())
            }

            if (content != null) {
                div(classes = "toggleable-content") {
                    content.invoke(this)
                }
            }
        }
    }
}