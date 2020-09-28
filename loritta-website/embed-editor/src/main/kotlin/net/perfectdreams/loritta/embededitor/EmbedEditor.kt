package net.perfectdreams.loritta.embededitor

import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onInputFunction
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.data.DiscordMessage
import net.perfectdreams.loritta.embededitor.data.crosswindow.*
import net.perfectdreams.loritta.embededitor.editors.*
import net.perfectdreams.loritta.embededitor.utils.MessageTagSection
import net.perfectdreams.loritta.embededitor.utils.ShowdownConverter
import net.perfectdreams.loritta.embededitor.utils.discordH5Heading
import net.perfectdreams.loritta.embededitor.utils.lovelyButton
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.MessageEvent
import org.w3c.dom.Window
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.clear

class EmbedEditor {
    var activeMessage: DiscordMessage? = null
    val json = Json(
            JsonConfiguration(
                    prettyPrint = true,
                    encodeDefaults = false,
                    indent = "  ",
                    ignoreUnknownKeys = true
            )
    )
    val markdownConverter = ShowdownConverter().apply {
        setOption("simpleLineBreaks", true)
        setOption("strikethrough", true)
    }
    var placeholders: List<Placeholder> = listOf()
    var isInEditMode = true
    var connectedViaExternalSources = false

    fun start() {
        /* window.addEventListener("keydown", { event ->
            event as KeyboardEvent

            println(event.key)

            if (event.key == "Control") {
                isInEditMode = !isInEditMode
                generateMessageAndUpdateJson(activeMessage!!, isInEditMode)
            }
        }) */

        activeMessage = DiscordMessage("OwO whats this?")

        val body = document.body!!

        body.addClass("theme-light")
        body.append {
            div {
                id = "content-wrapper"

                discordH5Heading("Preview da Mensagem")

                div {
                    id = "message-preview"
                }

                div {
                    discordH5Heading("Código JSON")

                    textArea(classes = "text-input inputDefault-_djjkz input-cIJ7To textArea-1Lj-Ns scrollbarDefault-3COgCQ scrollbar-3dvm_9") {
                        id = "json-code"
                        style = "resize: vertical; min-height: 100px;"

                        onInputFunction = {
                            // Load from JSON
                            parseAndLoadFromJson((it.target as HTMLTextAreaElement).value)
                        }
                    }
                }
            }
        }

        generateMessageAndUpdateJson(activeMessage!!)

        val opener = window.opener as Window?
        println("Opener is... something")
        println("Is it null? ${opener == null}")

        if (opener != null) {
            println("Sending ready packet to opener")
            opener.postMessage(
                    EmbedEditorCrossWindow.communicationJson.stringify(PacketWrapper.serializer(),
                            PacketWrapper(
                                    ReadyPacket()
                            )
                    ),
                    "*"
            )

            window.addEventListener("message", { event ->
                event as MessageEvent

                println("Received message ${event.data}")

                if (event.source == opener) {
                    println("Received message from our target source, yay!")

                    val packetWrapper = EmbedEditorCrossWindow.communicationJson.parse(PacketWrapper.serializer(), event.data as String)
                    val packet = packetWrapper.m

                    if (packet is MessageSetupPacket) {
                        placeholders = packet.placeholders

                        generateMessageAndUpdateJson(packet.message) // Load our cool message

                        connectedViaExternalSources = true
                    }
                }
            })
        }
    }

    fun parseAndLoadFromJson(rawJson: String) {
        val result = json.parse(DiscordMessage.serializer(), rawJson)
        generateMessageAndUpdateJson(result)
    }

    fun generateMessageAndUpdateJson(discordMessage: DiscordMessage, editMode: Boolean = isInEditMode) {
        this.activeMessage = discordMessage

        val editMessageTags = if (editMode)
            mapOf(
                    MessageTagSection.EMBED_AUTHOR_NOT_NULL to EmbedAuthorEditor.isNotNull,
                    MessageTagSection.EMBED_AUTHOR_NULL to EmbedAuthorEditor.isNull,

                    MessageTagSection.EMBED_DESCRIPTION_NOT_NULL to EmbedDescriptionEditor.isNotNull,
                    MessageTagSection.EMBED_DESCRIPTION_NULL to EmbedDescriptionEditor.isNull,

                    MessageTagSection.EMBED_TITLE_NOT_NULL to EmbedTitleEditor.isNotNull,
                    MessageTagSection.EMBED_TITLE_NULL to EmbedTitleEditor.isNull,

                    MessageTagSection.EMBED_FOOTER_NOT_NULL to EmbedFooterEditor.isNotNull,
                    MessageTagSection.EMBED_FOOTER_NULL to EmbedFooterEditor.isNull,

                    MessageTagSection.EMBED_IMAGE_NOT_NULL to EmbedImageEditor.isNotNull,
                    MessageTagSection.EMBED_IMAGE_NULL to EmbedImageEditor.isNull,

                    MessageTagSection.EMBED_THUMBNAIL_NOT_NULL to EmbedThumbnailEditor.isNotNull,
                    MessageTagSection.EMBED_THUMBNAIL_NULL to EmbedThumbnailEditor.isNull,

                    MessageTagSection.EMBED_FIELDS_FIELD to EmbedFieldEditor.changeField,
                    MessageTagSection.EMBED_AFTER_FIELDS to EmbedFieldEditor.addMoreFields,

                    MessageTagSection.EMBED_PILL to EmbedPillEditor.pillCallback,
                    MessageTagSection.MESSAGE_CONTENT to MessageContentEditor.changeContent
            )
        else mapOf()

        document.select<HTMLDivElement>("#message-preview").apply {
            clear()
            append {
                div {
                    style = "background-color: white;"
                    val renderer = EmbedRenderer(discordMessage, placeholders)
                    renderer.generateMessagePreview(
                            this
                    ) { currentElement, tag, renderInfo ->
                        attributes["message-tag-section"] = tag.name

                        editMessageTags[tag]?.invoke(this, this@EmbedEditor, discordMessage, currentElement, renderInfo)
                    }

                    if (editMode) {
                        if (discordMessage.embed != null) {
                            lovelyButton("fas fa-times", "Remover Embed", "red") {
                                generateMessageAndUpdateJson(
                                        activeMessage!!.copy(
                                                embed = null
                                        )
                                )
                            }
                        } else {
                            lovelyButton("far fa-window-maximize", "Adicionar Embed") {
                                generateMessageAndUpdateJson(
                                        activeMessage!!.copy(
                                                embed = DiscordEmbed()
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        document.select<HTMLTextAreaElement>("#json-code").value = json.stringify(DiscordMessage.serializer(), discordMessage)

        if (connectedViaExternalSources) {
            val opener = window.opener as Window?
            opener?.postMessage(
                    EmbedEditorCrossWindow.communicationJson.stringify(
                            PacketWrapper.serializer(),
                            PacketWrapper(
                                    UpdatedMessagePacket(
                                            json.stringify(
                                                    DiscordMessage.serializer(),
                                                    discordMessage
                                            )
                                    )
                            )
                    ),
                    "*"
            )
        }
    }

    fun parseDiscordText(text: String, parseMarkdown: Boolean = true, convertDiscordEmotes: Boolean = true, parsePlaceholders: Boolean = true): String {
        var output = text

        if (parseMarkdown) {
            output = markdownConverter.makeHtml(output)
        }

        if (parsePlaceholders) {
            for (placeholder in placeholders) {
                output = when (placeholder.renderType) {
                    RenderType.TEXT -> output.replace(placeholder.name, placeholder.replaceWith)
                    RenderType.MENTION -> output.replace(
                            placeholder.name,
                            createHTML().span(classes = "mention wrapper-3WhCwL mention interactive") {
                                + placeholder.replaceWith
                            }
                    )
                }
            }
        }

        if (convertDiscordEmotes) {
            // EMOTES
            // Nós fazemos uma vez antes e depois uma depois, para evitar bugs (já que :emoji: também existe dentro de <:emoji:...>
            val regex = Regex("<(a)?:([A-z0-9_-]+):([0-9]+)>", RegexOption.MULTILINE)
            output = regex.replace(output) { matchResult: MatchResult ->
                // <img class="inline-emoji" src="https://cdn.discordapp.com/emojis/$2.png?v=1">
                val extension = if (matchResult.groups[1]?.value == "a")
                    "gif"
                else
                    "png"
                "<img class=\"inline-emoji\" src=\"https://cdn.discordapp.com/emojis/${matchResult.groups[3]?.value}.$extension?v=1\">"
            }
        }

        return output
    }

    fun HTMLTag.parseAndAppendDiscordText(text: String, parseMarkdown: Boolean = true, convertDiscordEmotes: Boolean = true) {
        unsafe {
            raw(parseDiscordText(text, parseMarkdown, convertDiscordEmotes))
        }
    }

    fun parseAndAppendDiscordText(content: HTMLTag, text: String, parseMarkdown: Boolean = true, convertDiscordEmotes: Boolean = true) {
        content.unsafe {
            raw(parseDiscordText(text, parseMarkdown, convertDiscordEmotes))
        }
    }
}