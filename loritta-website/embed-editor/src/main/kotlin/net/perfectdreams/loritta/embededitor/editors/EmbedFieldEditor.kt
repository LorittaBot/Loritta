package net.perfectdreams.loritta.embededitor.editors

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import net.perfectdreams.loritta.embededitor.EmbedEditor
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.data.FieldRenderInfo
import net.perfectdreams.loritta.embededitor.generator.EmbedFieldsGenerator
import net.perfectdreams.loritta.embededitor.select
import net.perfectdreams.loritta.embededitor.utils.*
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import kotlin.browser.document

object EmbedFieldEditor : EditorBase {
    val changeField: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        currentElement.classes += "clickable"

        renderInfo as FieldRenderInfo

        currentElement.onClickFunction = {
            fieldPopup(
                    discordMessage.embed!!,
                    renderInfo.field,
                    false,
                    m
            )
        }
    }

    val addMoreFields: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        val embed = discordMessage.embed!!

        if (DiscordEmbed.MAX_FIELD_OBJECTS > embed.fields.size) {
            lovelyButton(
                    "fas fa-grip-lines",
                    "Adicionar Campo"
            ) {
                fieldPopup(
                        embed,
                        DiscordEmbed.Field("owo", "uwu"),
                        true,
                        m
                )
            }
        }
    }

    fun fieldPopup(embed: DiscordEmbed, field: DiscordEmbed.Field, isNew: Boolean, m: EmbedEditor) {
        val modal = TingleModal(
                TingleOptions(
                        footer = true
                )
        )

        modal.setContent(
                document.create.div {
                    div {
                        discordTextArea(m, field.name, "field-name", DiscordEmbed.MAX_FIELD_NAME_LENGTH)
                    }
                    div {
                        discordTextArea(m, field.value, "field-value", DiscordEmbed.MAX_FIELD_VALUE_LENGTH)
                    }
                    label {
                        + "Inline? "
                    }
                    input(InputType.checkBox) {
                        name = "field-inline"
                        checked = field.inline
                    }
                }
        )

        modal.closeMessageButton(
                m
        ) {
            m.activeMessage!!.copy(
                    embed = m.activeMessage!!.embed!!
                            .copy(
                                    fields = m.activeMessage!!.embed!!.fields.toMutableList().apply {
                                        remove(field)
                                    }
                            )
            )
        }

        modal.addLovelyFooterButton(
                "fas fa-save",
                "Salvar"
        ) {
            m.generateMessageAndUpdateJson(
                    if (isNew) {
                        m.activeMessage!!.copy(
                                embed = m.activeMessage!!.embed!!
                                        .copy(
                                                fields = m.activeMessage!!.embed!!.fields.toMutableList().apply {
                                                    add(
                                                            DiscordEmbed.Field(
                                                                    visibleModal.select<HTMLTextAreaElement>("[name='field-name']")
                                                                            .value,
                                                                    visibleModal.select<HTMLTextAreaElement>("[name='field-value']")
                                                                            .value,
                                                                    visibleModal.select<HTMLInputElement>("[name='field-inline']")
                                                                            .checked
                                                            )
                                                    )
                                                }
                                        )
                        )
                    } else {
                        m.activeMessage!!.copy(
                                embed = m.activeMessage!!.embed!!
                                        .copy(
                                                fields = m.activeMessage!!.embed!!.fields.toMutableList().apply {
                                                    val indexOf = embed.fields.indexOf(field)
                                                    remove(field)

                                                    add(
                                                            indexOf,
                                                            DiscordEmbed.Field(
                                                                    visibleModal.select<HTMLTextAreaElement>("[name='field-name']")
                                                                            .value,
                                                                    visibleModal.select<HTMLTextAreaElement>("[name='field-value']")
                                                                            .value,
                                                                    visibleModal.select<HTMLInputElement>("[name='field-inline']")
                                                                            .checked
                                                            )
                                                    )
                                                }
                                        )
                        )
                    }
            )

            modal.close()
        }

        modal.open()
        // visibleModal.select<HTMLTextAreaElement>(".text-input").autoResize()
    }
}