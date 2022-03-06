package net.perfectdreams.loritta.embededitor.editors

import kotlinx.browser.document
import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.dom.create
import net.perfectdreams.loritta.embededitor.EmbedEditor
import net.perfectdreams.loritta.embededitor.data.AdditionalRenderInfo
import net.perfectdreams.loritta.embededitor.data.DiscordMessage
import net.perfectdreams.loritta.embededitor.select
import net.perfectdreams.loritta.embededitor.utils.TingleModal
import net.perfectdreams.loritta.embededitor.utils.TingleOptions
import net.perfectdreams.loritta.embededitor.utils.addLovelyFooterButton
import net.perfectdreams.loritta.embededitor.utils.autoResize
import net.perfectdreams.loritta.embededitor.utils.discordTextArea
import net.perfectdreams.loritta.embededitor.utils.discordTextInput
import net.perfectdreams.loritta.embededitor.utils.visibleModal
import org.w3c.dom.HTMLTextAreaElement

typealias ELEMENT_CONFIGURATION = (FlowContent.(m: EmbedEditor, discordMessage: DiscordMessage, currentElement: CommonAttributeGroupFacade, renderInfo: AdditionalRenderInfo?) -> (Unit))

interface EditorBase {
    fun genericPopupTextInputWithSaveAndDelete(
            m: EmbedEditor,
            onSaveNewMessage: (String) -> (DiscordMessage),
            onDeleteNewMessage: () -> (DiscordMessage),
            text: String,
            maxLength: Int
    ) {
        val modal = TingleModal(
                TingleOptions(
                        footer = true
                )
        )

        modal.setContent(
                document.create.div {
                    div {
                        discordTextInput(m, text, "generic-text", maxLength)
                    }
                }
        )

        modal.closeMessageButton(
                m,
                onDeleteNewMessage
        )

        modal.saveMessageButton(
                m,
                onSaveNewMessage
        )

        modal.open()
        visibleModal.select<HTMLTextAreaElement>(".text-input").autoResize()
    }

    fun genericPopupTextAreaWithSaveAndDelete(
            m: EmbedEditor,
            onSaveNewMessage: (String) -> (DiscordMessage),
            onDeleteNewMessage: () -> (DiscordMessage),
            text: String,
            maxLength: Int
    ) {
        val modal = TingleModal(
                TingleOptions(
                        footer = true
                )
        )

        modal.setContent(
                document.create.div {
                    div {
                        discordTextArea(m, text, "generic-text", maxLength)
                    }
                }
        )

        modal.closeMessageButton(
                m,
                onDeleteNewMessage
        )

        modal.saveMessageButton(
                m,
                onSaveNewMessage
        )

        modal.open()
        visibleModal.select<HTMLTextAreaElement>(".text-input").autoResize()
    }

    fun TingleModal.closeMessageButton(m: EmbedEditor, onDeleteNewMessage: () -> (DiscordMessage)) {
        this.addLovelyFooterButton(
                "fas fa-times",
                "Remover",
                "red"
        ) {
            m.generateMessageAndUpdateJson(
                    onDeleteNewMessage.invoke()
            )

            this.close()
        }
    }

    fun TingleModal.saveMessageButton(m: EmbedEditor, onSaveNewMessage: (String) -> (DiscordMessage)) {
        this.addLovelyFooterButton(
                "fas fa-save",
                "Salvar"
        ) {
            m.generateMessageAndUpdateJson(
                    onSaveNewMessage.invoke(visibleModal.select<HTMLTextAreaElement>("[name='generic-text']").value)
            )

            this.close()
        }
    }
}