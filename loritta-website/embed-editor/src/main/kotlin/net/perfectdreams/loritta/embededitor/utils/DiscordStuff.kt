package net.perfectdreams.loritta.embededitor.utils

import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onInputFunction
import kotlinx.html.stream.createHTML
import net.perfectdreams.loritta.embededitor.EmbedEditor
import net.perfectdreams.loritta.embededitor.select
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import kotlin.dom.clear

fun FlowContent.discordTextInput(
        m: EmbedEditor,
        text: String?,
        name: String,
        maxLength: Int? = null,
        enableTextPreview: Boolean = true
) {
    fun HTMLInputElement.updateMaxLength() {
        val text = this.value
        this.parentElement!!.select<HTMLDivElement>(".text-max-length")
                .innerText = ((maxLength ?: 0) - text.length).toString()
    }

    fun HTMLInputElement.updateTextPreview() {
        val text = this.value
        val textPreviewDiv = this.parentElement!!.parentElement!!.parentElement!!.select<HTMLDivElement>(".text-preview")
        textPreviewDiv.clear()
        textPreviewDiv.append {
            div {
                m.parseAndAppendDiscordText(this, text)
            }
        }
    }

    div(classes = "inputWrapper-31_8H8") {
        div(classes = "inputMaxLength-1vRluy") {
            input(classes = "text-input inputDefault-_djjkz input-cIJ7To scrollbarDefault-3COgCQ scrollbar-3dvm_9") {
                this.maxLength = maxLength.toString()
                this.name = name

                onInputFunction = {
                    if (maxLength != null)
                        (it.target as HTMLInputElement).updateMaxLength()

                    if (enableTextPreview)
                        (it.target as HTMLInputElement).updateTextPreview()
                }

                if (text != null)
                    value = text
            }

            div(classes = "text-max-length maxLength-39QFBo") {
                + maxLength.toString()
            }
        }
    }
}

fun FlowContent.discordTextArea(
        m: EmbedEditor,
        text: String,
        name: String,
        maxLength: Int? = null,
        enableTextPreview: Boolean = true
) {
    fun HTMLTextAreaElement.updateMaxLength() {
        val text = this.value
        this.parentElement!!.select<HTMLDivElement>(".text-max-length")
                .innerText = ((maxLength ?: 0) - text.length).toString()
    }

    fun HTMLTextAreaElement.updateTextPreview() {
        val text = this.value
        val textPreviewDiv = this.parentElement!!.parentElement!!.parentElement!!.select<HTMLDivElement>(".text-preview")
        textPreviewDiv.clear()
        textPreviewDiv.append {
            div {
                m.parseAndAppendDiscordText(this, text)
            }
        }
    }

    div {
        div(classes = "inputWrapper-31_8H8") {
            div(classes = "inputMaxLength-1vRluy") {
                textArea(classes = "text-input inputDefault-_djjkz input-cIJ7To textArea-1Lj-Ns scrollbarDefault-3COgCQ scrollbar-3dvm_9") {
                    this.maxLength = maxLength.toString()
                    this.name = name

                    onInputFunction = {
                        if (maxLength != null)
                            (it.target as HTMLTextAreaElement).updateMaxLength()

                        if (enableTextPreview)
                            (it.target as HTMLTextAreaElement).updateTextPreview()
                    }

                    +text
                }

                div(classes = "text-max-length maxLength-39QFBo") {
                    if (maxLength != null) {
                        +(maxLength - text.length).toString()
                    }
                }
            }
        }
        div(classes = "text-preview") {}
    }
}

fun FlowContent.discordH2Heading(text: String) {
    h2(classes = "colorStandard-2KCXvj size14-e6ZScH h2-2gWE-o title-3sZWYQ defaultColor-1_ajX0 defaultMarginh2-2LTaUL") {
        + text
    }
}

fun FlowContent.discordH5Heading(text: String) {
    h5(classes = "colorStandard-2KCXvj size14-e6ZScH h5-18_1nd title-3sZWYQ marginBottom4-2qk4Hy") {
        + text
    }
}

fun FlowContent.lovelyButton(icon: String? = null, text: String, color: String = "primary", onClick: ((Event) -> (Unit))? = null) {
    div(classes = "button $color") {
        if (icon != null) {
            i(classes = icon)
            + " "
        }

        + text

        if (onClick != null)
            onClickFunction = onClick
    }
}

fun TingleModal.addLovelyFooterButton(icon: String? = null, text: String, color: String = "primary", onClick: (() -> (Unit))? = null) {
    this.addFooterBtn(
            createHTML().span {
                if (icon != null) {
                    i(classes = icon)
                    +" "
                }

                +text
            },
            "button modal-button $color",
            onClick ?: {}
    )
}