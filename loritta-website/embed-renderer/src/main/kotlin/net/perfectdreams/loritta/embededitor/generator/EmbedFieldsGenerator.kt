package net.perfectdreams.loritta.embededitor.generator

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.style
import net.perfectdreams.loritta.embededitor.EmbedRenderer
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.data.FieldRenderInfo
import net.perfectdreams.loritta.embededitor.utils.MessageTagSection

object EmbedFieldsGenerator : GeneratorBase {
    fun generate(m: EmbedRenderer, content: FlowContent, embed: DiscordEmbed, modifyTagCallback: MODIFY_TAG_CALLBACK? = null) {
        if (embed.fields.isNotEmpty()) {
            content.div(classes = "embedFields-2IPs5Z") {
                // Rendering *inline* fields is hard as fucc
                // We know that there can be at *maximum* three inline fields in a row in a embed
                // So, if we want to place everything nicely, we need to keep track of the previous and next
                // inline fields.
                // After all...
                // [inline field]
                // [inline field]
                // [field]
                // [inline field]
                // [inline field]
                // [inline field]
                // should be displayed as
                // [inline field] [inline field]
                // [field]
                // [inline field] [inline field] [inline field]
                // So, to do that, let's split up everything in different chunks, inlined and non inlined chunks
                val chunks = mutableListOf<MutableList<DiscordEmbed.Field>>()

                for (field in embed.fields) {
                    val currentChunk = chunks.lastOrNull() ?: run {
                        val newList = mutableListOf<DiscordEmbed.Field>()
                        chunks.add(newList)
                        newList
                    }

                    if (currentChunk.firstOrNull()?.inline != field.inline) {
                        // New chunk needs to be created!
                        val newList = mutableListOf<DiscordEmbed.Field>()
                        newList.add(field)
                        chunks.add(newList)
                    } else {
                        // Same type, so we are going to append to the current chunk
                        currentChunk.add(field)
                    }
                }

                var fieldIndex = 0
                for (fieldChunk in chunks) {
                    // Because fields are grouped by three, we are going to chunk again
                    val groupedFields = fieldChunk.chunked(3)

                    for (fieldGroup in groupedFields) {
                        for ((index, field) in fieldGroup.withIndex()) {
                            div(classes = "embedField-1v-Pnh") {
                                modifyTagCallback?.invoke(
                                        this,
                                        this,
                                        MessageTagSection.EMBED_FIELDS_FIELD,
                                        FieldRenderInfo(
                                                fieldIndex,
                                                field
                                        )
                                )

                                style = if (!field.inline) "grid-column: 1 / 13;" else {
                                    if (fieldGroup.size == 3) {
                                        when (index) {
                                            2 -> "grid-column: 9 / 13;"
                                            1 -> "grid-column: 5 / 9;"
                                            else -> "grid-column: 1 / 5;"
                                        }
                                    } else {
                                        when (index) {
                                            1 -> "grid-column: 7 / 13;"
                                            else -> "grid-column: 1 / 7;"
                                        }
                                    }
                                }

                                div(classes = "embedFieldName-NFrena") {
                                    m.parseAndAppendDiscordText(this, field.name)
                                }

                                div(classes = "embedFieldValue-nELq2s") {
                                    m.parseAndAppendDiscordText(this, field.value)
                                }
                            }
                            fieldIndex++
                        }
                    }
                }
            }
        }

        content.div {
            modifyTagCallback?.invoke(
                    this,
                    this,
                    MessageTagSection.EMBED_AFTER_FIELDS,
                    null
            )
        }
    }
}