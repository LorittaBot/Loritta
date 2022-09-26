package net.perfectdreams.loritta.embededitor.editors

import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import net.perfectdreams.loritta.embededitor.select
import net.perfectdreams.loritta.embededitor.utils.*
import org.w3c.dom.HTMLTextAreaElement
import kotlinx.browser.document

object MessageContentEditor : EditorBase {
    val changeContent: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        currentElement.classes += "clickable inline-flex"

        currentElement.onClickFunction = {
            val modal = TingleModal(
                    TingleOptions(
                            footer = true
                    )
            )

            modal.setContent(
                    document.create.div {
                        div {
                            discordTextArea(m, discordMessage.content, "message-content", 2000)
                        }
                    }
            )

            modal.addFooterBtn("<i class=\"fas fa-times\"></i> Deletar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
                m.generateMessageAndUpdateJson(
                        m.activeMessage!!.copy(
                                embed = m.activeMessage!!.embed!!.copy(description = null)
                        )
                )

                modal.close()
            }

            modal.addFooterBtn("<i class=\"fas fa-times\"></i> Salvar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
                m.generateMessageAndUpdateJson(
                        m.activeMessage!!.copy(
                                content = visibleModal.select<HTMLTextAreaElement>("[name='message-content']").value
                        )
                )

                modal.close()
            }

            modal.open()
            visibleModal.select<HTMLTextAreaElement>(".text-input").autoResize()
            // modal.trackOverflowChanges(m)
        }
    }
}