package net.perfectdreams.loritta.embededitor.editors

import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import net.perfectdreams.loritta.embededitor.EmbedEditor
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.select
import net.perfectdreams.loritta.embededitor.utils.*
import org.w3c.dom.HTMLTextAreaElement
import kotlin.browser.document

object EmbedFooterEditor : EditorBase {
    val isNotNull: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        currentElement.classes += "clickable"

        currentElement.onClickFunction = {
            footerPopup(discordMessage.embed!!, m)
        }
    }

    val isNull: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        lovelyButton(
                "fas fa-at",
                "Adicionar Rodapé"
        ) {
            footerPopup(discordMessage.embed!!, m)
        }
    }

    fun footerPopup(embed: DiscordEmbed, m: EmbedEditor) {
        val modal = TingleModal(
                TingleOptions(
                        footer = true
                )
        )

        val footer = embed.footer

        modal.setContent(
                document.create.div {
                    discordH2Heading("Rodapé")

                    discordH5Heading("Texto do Rodapé")
                    div {
                        discordTextArea(m, footer?.text ?: "yay", "footer-text", DiscordEmbed.MAX_AUTHOR_NAME_LENGTH)
                    }
                    discordH5Heading("Imagem do Rodapé (Pode ser vazio)")
                    div {
                        discordTextInput(m, footer?.iconUrl, "footer-image-url")
                    }
                }
        )

        modal.closeMessageButton(
                m
        ) {
            m.activeMessage!!.copy(
                    embed = m.activeMessage!!.embed!!
                            .copy(
                                    footer = null
                            )
            )
        }

        modal.addLovelyFooterButton(
                "fas fa-save",
                "Salvar"
        ) {
            m.generateMessageAndUpdateJson(
                    m.activeMessage!!.copy(
                            embed = m.activeMessage!!.embed!!
                                    .copy(
                                            footer = DiscordEmbed.Footer(
                                                    text = visibleModal.select<HTMLTextAreaElement>("[name='footer-text']")
                                                            .value,
                                                    iconUrl = visibleModal.select<HTMLTextAreaElement>("[name='footer-image-url']")
                                                            .value.ifBlank { null }
                                            )
                                    )
                    )
            )

            modal.close()
        }

        modal.open()
        // visibleModal.select<HTMLTextAreaElement>(".text-input").autoResize()
    }
}