package net.perfectdreams.dora.components

import kotlinx.html.*
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.Translator
import net.perfectdreams.dora.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import java.util.*

fun FlowContent.batchEntry(
    project: Project,
    uniqueId: String,
    languageId: String,
    keyId: String,
    context: String?,
    transformers: List<String>?,
    originalText: String,
    translatedText: String?,
    isTranslated: Boolean,
    approvedBy: Translator?,
    editorOpen: Boolean
) {
    div {
        id = "entry-area-$uniqueId"

        div(classes = "translation-table-entry-header") {
            div(classes = "section-header-grid") {
                div(classes = "section-icon") {
                    svgIcon(SVGIcons.Key)
                }

                div {
                    text(keyId)
                }

                if (isTranslated) {
                    if (approvedBy != null) {
                        img(src = approvedBy.effectiveAvatarUrl, alt = approvedBy.name, classes = "section-icon") {
                            style = "width: 16px; height: 16px; border-radius: 99999px;"
                        }

                        div {
                            text(approvedBy.name)
                        }
                    } else {
                        div(classes = "section-icon") {
                            svgIcon(SVGIcons.Robot)
                        }

                        div {
                            text("Machine Translation")
                        }
                    }
                }

                if (context != null) {
                    div(classes = "section-icon") {
                        svgIcon(SVGIcons.NotePencil)
                    }

                    div {
                        text(context)
                    }
                }

                if (transformers != null) {
                    div(classes = "section-icon") {
                        svgIcon(SVGIcons.CursorText)
                    }

                    div {
                        text(transformers.joinToString(", "))
                    }
                }
            }
        }

        div {
            style = "display: grid; grid-template-columns: 1fr 1fr;"

            div(classes = "translation-table-entry-text") {
                style = "background-color: #f5f7fd;"
                transformedDiscordText(originalText)
            }

            div(classes = "translation-table-entry-text") {
                if (isTranslated)
                    style = "background-color: #efffe3;"

                if (editorOpen) {
                    textArea {
                        id = "entry-text-$uniqueId"

                        name = "translatedText"
                        text(translatedText ?: originalText)
                    }

                    discordButton(ButtonStyle.PRIMARY) {
                        attributes["bliss-include-json"] = "#entry-text-$uniqueId"
                        attributes["bliss-put"] = "/projects/${project.slug}/languages/$languageId/table/$keyId/$uniqueId"
                        attributes["bliss-swap:200"] = "#entry-area-$uniqueId (outerHTML) -> #entry-area-$uniqueId (outerHTML)"
                        text("Salvar")
                    }

                    if (isTranslated) {
                        discordButton(ButtonStyle.DANGER) {
                            attributes["bliss-delete"] = "/projects/${project.slug}/languages/$languageId/table/$keyId/$uniqueId"
                            attributes["bliss-swap:200"] = "#entry-area-$uniqueId (outerHTML) -> #entry-area-$uniqueId (outerHTML)"
                            text("Excluir Tradução")
                        }
                    }

                    discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                        attributes["bliss-get"] = "/projects/${project.slug}/languages/$languageId/table/$keyId/$uniqueId"
                        attributes["bliss-swap:200"] = "#entry-area-$uniqueId (outerHTML) -> #entry-area-$uniqueId (outerHTML)"
                        text("Cancelar")
                    }
                } else {
                    transformedDiscordText(translatedText ?: originalText)

                    div(classes = "translation-controls") {
                        if (!isTranslated) {
                            hiddenInput {
                                id = "entry-text-$uniqueId"
                                name = "translatedText"
                                value = translatedText ?: originalText
                            }

                            discordButton(ButtonStyle.PRIMARY) {
                                attributes["bliss-include-json"] = "#entry-text-$uniqueId"
                                attributes["bliss-put"] = "/projects/${project.slug}/languages/$languageId/table/$keyId/$uniqueId"
                                attributes["bliss-swap:200"] = "#entry-area-$uniqueId (outerHTML) -> #entry-area-$uniqueId (outerHTML)"

                                text("Aprovar")
                            }
                        } else {
                            // When we do have a translation, allow deleting it
                            discordButton(ButtonStyle.DANGER) {
                                attributes["bliss-delete"] = "/projects/${project.slug}/languages/$languageId/table/$keyId/$uniqueId"
                                attributes["bliss-swap:200"] = "#entry-area-$uniqueId (outerHTML) -> #entry-area-$uniqueId (outerHTML)"
                                text("Excluir Tradução")
                            }
                        }

                        discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                            attributes["bliss-get"] = "/projects/${project.slug}/languages/$languageId/table/${keyId}/$uniqueId/editor"
                            attributes["bliss-swap:200"] = "body (innerHTML) -> #entry-area-$uniqueId (outerHTML)"

                            text("Editar")
                        }
                    }
                }
            }
        }
    }
}