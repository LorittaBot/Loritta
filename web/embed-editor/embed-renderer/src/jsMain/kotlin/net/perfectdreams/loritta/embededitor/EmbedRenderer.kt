package net.perfectdreams.loritta.embededitor

import kotlinx.browser.document
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.embededitor.data.DiscordMessage
import net.perfectdreams.loritta.embededitor.data.crosswindow.Placeholder
import net.perfectdreams.loritta.embededitor.data.crosswindow.RenderType
import net.perfectdreams.loritta.embededitor.generator.*
import net.perfectdreams.loritta.embededitor.utils.ShowdownConverter

class EmbedRenderer(val message: DiscordMessage, val placeholders: List<Placeholder>) {
    companion object {
        val json = Json {
            prettyPrint = true
            encodeDefaults = false
            prettyPrintIndent = "  "
            ignoreUnknownKeys = true
        }

        val markdownConverter = ShowdownConverter().apply {
            setOption("simpleLineBreaks", true)
            setOption("strikethrough", true)
        }
    }

    fun generateMessagePreview(content: FlowContent, modifyTagCallback: MODIFY_TAG_CALLBACK? = null) {
        content.div(classes = "message-2qnXI6 cozyMessage-3V1Y8y groupStart-23k01U wrapper-2a6GCs cozy-3raOZG zalgo-jN1Ica") {
            div(classes = "contents-2mQqc9") {
                img(classes = "avatar-1BDn8e", src = "https://loritta.website/assets/img/lori_avatar_v4.png")

                h2(classes = "header-23xsNx") {
                    span(classes = "username-1A8OIy") {
                        style = "color: rgb(233, 30, 99);"

                        +"Loritta Morenitta \uD83D\uDE18"
                    }
                    span(classes = "botTagCozy-1fFsZk botTag-1un5a6 botTagRegular-2HEhHi botTag-2WPJ74 rem-2m9HGf") {
                        span(classes = "botText-1526X_") {
                            +"BOT"
                        }
                    }
                    span(classes = "timestamp-3ZCmNB") {
                        +"Today at 09:07"
                    }
                }

                MessageContentGenerator.generate(this@EmbedRenderer, this, message, modifyTagCallback)
            }

            val embed = message.embed
            if (embed != null) {
                div(classes = "container-1ov-mD") {
                    div(classes = "embedWrapper-lXpS3L embedFull-2tM8-- embed-IeVjo6 markup-2BOw-j") {
                        style = "display: flex;"

                        EmbedPillGenerator.generate(this@EmbedRenderer, this, embed, modifyTagCallback)

                        val thumbnailUrl = embed.thumbnail?.url

                        div(classes = "grid-1nZz7S") {
                            if (thumbnailUrl != null)
                                classes += "hasThumbnail-3FJf1w"

                            EmbedAuthorGenerator.generate(this@EmbedRenderer, this, embed, modifyTagCallback)
                            EmbedTitleGenerator.generate(this@EmbedRenderer, this, embed, modifyTagCallback)
                            EmbedDescriptionGenerator.generate(this@EmbedRenderer, this, embed, modifyTagCallback)
                            EmbedFieldsGenerator.generate(this@EmbedRenderer, this, embed, modifyTagCallback)
                            EmbedImageGenerator.generate(this@EmbedRenderer, this, embed, modifyTagCallback)
                            EmbedThumbnailGenerator.generate(this@EmbedRenderer, this, embed, modifyTagCallback)
                            EmbedFooterGenerator.generate(this@EmbedRenderer, this, embed, modifyTagCallback)
                        }
                    }
                }
            }
        }
    }

    fun parseDiscordText(text: String, parseMarkdown: Boolean = true, convertDiscordEmotes: Boolean = true, parsePlaceholders: Boolean = true): String {
        var output = stripHtmlTagsUsingDom(text)

        // Strip all
        if (parseMarkdown)
            output = markdownConverter.makeHtml(output)

        if (parsePlaceholders)
            output = parsePlaceholders(output)

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

    fun parsePlaceholders(text: String): String {
        var output = text
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
        return output
    }

    private fun HTMLTag.parseAndAppendDiscordText(text: String, parseMarkdown: Boolean = true, convertDiscordEmotes: Boolean = true) {
        unsafe {
            raw(parseDiscordText(text, parseMarkdown, convertDiscordEmotes))
        }
    }

    fun parseAndAppendDiscordText(content: HTMLTag, text: String, parseMarkdown: Boolean = true, convertDiscordEmotes: Boolean = true) {
        content.unsafe {
            raw(parseDiscordText(text, parseMarkdown, convertDiscordEmotes))
        }
    }

    fun stripHtmlTagsUsingDom(html: String): String {
        val div = document.createElement("div")
        div.innerHTML = html
        return div.textContent ?: ""
    }
}