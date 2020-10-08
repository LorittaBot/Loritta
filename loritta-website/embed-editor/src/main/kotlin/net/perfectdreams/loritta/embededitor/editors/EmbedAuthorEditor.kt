package net.perfectdreams.loritta.embededitor.editors

import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import net.perfectdreams.loritta.embededitor.EmbedEditor
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.data.DiscordMessage
import net.perfectdreams.loritta.embededitor.generator.EmbedAuthorGenerator
import net.perfectdreams.loritta.embededitor.select
import net.perfectdreams.loritta.embededitor.utils.*
import org.w3c.dom.HTMLTextAreaElement
import kotlin.browser.document

object EmbedAuthorEditor : EditorBase {
    val isNotNull: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        currentElement.classes += "clickable"

        currentElement.onClickFunction = {
            authorPopup(discordMessage.embed!!, m)
        }
    }

    val isNull: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        lovelyButton(
                "fas fa-at",
                "Adicionar Autor"
        ) {
            authorPopup(discordMessage.embed!!, m)
        }
    }

    fun authorPopup(embed: DiscordEmbed, m: EmbedEditor) {
        val modal = TingleModal(
                TingleOptions(
                        footer = true
                )
        )

        val author = embed.author

        modal.setContent(
                document.create.div {
                    discordH2Heading("Autor")

                    discordH5Heading("Nome do Autor")
                    div {
                        discordTextArea(m, author?.name ?: "yay", "author-name", DiscordEmbed.MAX_AUTHOR_NAME_LENGTH)
                    }
                    discordH5Heading("URL do Autor (Pode ser vazio)")
                    div {
                        discordTextInput(m, author?.url, "author-url")
                    }
                    discordH5Heading("Imagem do Autor (Pode ser vazio)")
                    div {
                        discordTextInput(m, author?.iconUrl, "author-image-url")
                    }
                }
        )

        modal.closeMessageButton(
                m
        ) {
            m.activeMessage!!.copy(
                    embed = m.activeMessage!!.embed!!
                            .copy(
                                    author = null
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
                                            author = DiscordEmbed.Author(
                                                    name = visibleModal.select<HTMLTextAreaElement>("[name='author-name']")
                                                            .value,
                                                    url = visibleModal.select<HTMLTextAreaElement>("[name='author-url']")
                                                            .value.ifBlank { null },
                                                    iconUrl = visibleModal.select<HTMLTextAreaElement>("[name='author-image-url']")
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