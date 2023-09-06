package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.common.embeds.DiscordEmbed
import net.perfectdreams.loritta.cinnamon.dashboard.common.embeds.DiscordMessage
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.DiscordMessageUtils
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.MessagePlaceholder
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.stripHTML
import net.perfectdreams.loritta.common.utils.Color
import net.perfectdreams.loritta.serializable.DiscordUser
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.compose.web.attributes.href
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.asList
import org.w3c.dom.parsing.DOMParser

/**
 * Renders a Discord message
 */
@Composable
fun DiscordMessageRenderer(
    author: DiscordUser,
    message: DiscordMessage,
    placeholders: List<MessagePlaceholder>
) {
    Div(attrs = {
        classes("discord-style")
        // TODO: Maybe create it as an "wrapper" or something? Sometimes we may want to render the message as is without any border and stuff
        attr("style", "background-color: white;\n" +
                "  border-radius: 7px;\n" +
                "  padding: 1em;\n" +
                "  overflow: hidden;\n" +
                "  border: 1px solid #e1e1e4;")
    }) {
        Div(attrs = {
            classes("theme-light")
        }) {
            Div(attrs = {
                classes("discord-message")
            }) {
                Div(attrs = {
                    classes("discord-message-sidebar")
                }) {
                    DiscordAvatar(
                        UserId(author.id),
                        author.discriminator,
                        author.avatarId
                    ) {
                        classes("discord-message-avatar")
                    }
                }
                Div(attrs = {
                    classes("discord-message-content")
                }) {
                    H2(attrs = {
                        classes("discord-message-header")
                    }) {
                        Span(attrs = {
                            classes("discord-message-username")
                            attr("style", "color: rgb(233, 30, 99);")
                        }) {
                            Text(author.name)
                        }
                        Span(attrs = {
                            classes("discord-message-bot-tag")
                        }) {
                            Text("BOT")
                        }
                        Span(attrs = {
                            classes("discord-message-timestamp")
                        }) {
                            Text("Today at 09:07")
                        }
                    }

                    // ===[ MESSAGE CONTENT ]===
                    Div(attrs = {
                        classes("markup-eYLPri", "messageContent-2t3eCI")
                    }) {
                        Span {
                            TransformedDiscordText(message.content, placeholders)
                        }
                    }

                    val embed = message.embed
                    // ===[ EMBED ]===
                    if (embed != null) {
                        val thumbnailUrl = embed.thumbnail?.url

                        Article(attrs = {
                            classes("discord-embed")

                            // ===[ EMBED PILL ]===
                            val embedColor = embed.color
                            if (embedColor != null) {
                                val aux = ("000000" + ((embedColor) ushr 0).toString(16))
                                val hex = "#" + aux.slice(aux.length - 6 until aux.length)
                                attr("style", "border-color: $hex;")
                            }
                        }) {
                            Div(attrs = {
                                classes("discord-embed-content")
                            }) {
                                // ===[ EMBED AUTHOR ]===
                                val author = embed.author
                                if (author != null) {
                                    Div(attrs = {
                                        classes("discord-embed-author")
                                    }) {
                                        val authorIconUrl = author.iconUrl
                                        if (authorIconUrl != null) {
                                            Img(src = DiscordMessageUtils.parsePlaceholdersToString(authorIconUrl, placeholders), attrs = {
                                                classes("discord-embed-icon")
                                            })
                                        }

                                        val authorUrl = author.url
                                        if (authorUrl != null) {
                                            A(
                                                href = DiscordMessageUtils.parsePlaceholdersToString(authorUrl, placeholders),
                                                attrs = {
                                                    classes("discord-embed-text")
                                                }
                                            ) {
                                                Text(DiscordMessageUtils.parsePlaceholdersToString(author.name, placeholders))
                                            }
                                        } else {
                                            Span(attrs = {
                                                classes("discord-embed-text")
                                            }) {
                                                Text(DiscordMessageUtils.parsePlaceholdersToString(author.name, placeholders))
                                            }
                                        }
                                    }
                                }

                                // ===[ EMBED TITLE ]===
                                val title = embed.title
                                if (title != null) {
                                    val titleUrl = embed.url
                                    if (titleUrl != null) {
                                        A(attrs = {
                                            href(titleUrl)
                                            classes("discord-embed-title")
                                        }) {
                                            TransformedDiscordText(title, placeholders)
                                        }
                                    } else {
                                        Div(attrs = {
                                            classes("discord-embed-title")
                                        }) {
                                            TransformedDiscordText(title, placeholders)
                                        }
                                    }
                                }

                                // ===[ EMBED DESCRIPTION ]===
                                val description = embed.description
                                if (description != null) {
                                    Div(attrs = {
                                        classes("discord-embed-description")
                                    }) {
                                        TransformedDiscordText(description, placeholders)
                                    }
                                }

                                if (embed.fields.isNotEmpty()) {
                                    Div(attrs = {
                                        classes("discord-embed-fields")
                                    }) {
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
                                                    Div(attrs = {
                                                        classes("discord-embed-field")
                                                        attr(
                                                            "style", if (!field.inline) "grid-column: 1 / 13;" else {
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
                                                        )
                                                    }) {
                                                        Div(attrs = {
                                                            classes("discord-embed-field-name")
                                                        }) {
                                                            TransformedDiscordText(field.name, placeholders)
                                                        }

                                                        Div(attrs = {
                                                            classes("discord-embed-field-value")
                                                        }) {
                                                            TransformedDiscordText(field.value, placeholders)
                                                        }
                                                    }
                                                    fieldIndex++
                                                }
                                            }
                                        }
                                    }
                                }

                                // ===[ EMBED IMAGE ]===
                                val imageUrl = embed.image?.url
                                if (imageUrl != null) {
                                    Div {
                                        Img(src = DiscordMessageUtils.parsePlaceholdersToString(imageUrl, placeholders)) {
                                            attr("style", "width: 100%;")
                                        }
                                    }
                                }

                                val footer = embed.footer
                                if (footer != null) {
                                    Div(attrs = {
                                        classes("discord-embed-footer")
                                    }) {
                                        val footerIconUrl = footer.iconUrl
                                        if (footerIconUrl != null) {
                                            Img(src = DiscordMessageUtils.parsePlaceholdersToString(footerIconUrl, placeholders)) {
                                                classes("embedFooterIcon-1dTZzD")
                                            }
                                        }

                                        Span(attrs = {
                                            classes("embedFooterText-2Mc7H9")
                                        }) {
                                            Text(DiscordMessageUtils.parsePlaceholdersToString(footer.text, placeholders))
                                        }
                                    }
                                }
                            }

                            // ===[ EMBED THUMBNAIL ]===
                            // This stays outside of the embed content
                            if (thumbnailUrl != null) {
                                A(attrs = {
                                    classes("discord-embed-thumbnail")
                                }) {
                                    Img(src = DiscordMessageUtils.parsePlaceholdersToString(thumbnailUrl, placeholders)) {
                                        attr("style", "width: 100%;")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransformedDiscordText(input: String, placeholders: List<MessagePlaceholder>) {
    val cleanInput = stripHTML(input)

    val convertedFromMarkdown = DiscordMessageUtils.showdown.makeHtml(cleanInput)

    val parsedToHTML = DOMParser().parseFromString(convertedFromMarkdown, "text/html")

    @Composable
    fun traverseNodesAndRender(element: Node) {
        console.log(element)

        // Loop thru the rendered HTML and render what we *really* want instead
        // Yes, we could parse the markdown to AST and then render based off that, but eh, this works too, even tho it feels bad
        // Discord also only uses some features from Markdown, so rendering them all manually isn't all that bad

        when (element.nodeType) {
            Node.TEXT_NODE -> {
                // This is a text node, so let's render Discord stuff like emotes, mentions, etc
                val textContent = element.textContent ?: return // Null, bail out!

                val sections = DiscordMessageUtils.parseStringToDrawableSections(textContent)

                for (section in sections) {
                    when (section) {
                        is DiscordMessageUtils.DrawableDiscordEmote -> {
                            val extension = if (section.animated)
                                "gif"
                            else
                                "png"
                            Img(src = "https://cdn.discordapp.com/emojis/${section.emoteId}.$extension?v=1", attrs = {
                                classes("discord-inline-emoji")
                            })
                        }
                        is DiscordMessageUtils.DrawableText -> Text(section.text)
                        is DiscordMessageUtils.DrawablePlaceholder -> {
                            val placeholder = placeholders.firstOrNull { it.name == section.placeholderName }

                            // TODO: Add a hover tooltip when you hover a placeholder, to show what placeholder triggers it
                            if (placeholder != null) {
                                when (placeholder.renderType) {
                                    MessagePlaceholder.RenderType.TEXT -> Text(placeholder.replaceWith)
                                    MessagePlaceholder.RenderType.MENTION -> InlineDiscordMention(placeholder.replaceWith)
                                }
                            } else {
                                // Unknown placeholder!
                                InlineDiscordMention("Placeholder inválido!", Color(237, 66, 69))
                            }
                        }
                    }
                }
            }
            Node.ELEMENT_NODE -> {
                val tagName = (element as? HTMLElement)?.tagName
                println(tagName)

                when (tagName) {
                    // **Bold**
                    "STRONG" -> {
                        B {
                            for (node in element.childNodes.asList()) {
                                traverseNodesAndRender(node)
                            }
                        }
                    }
                    // _Italic_
                    "EM" -> {
                        I {
                            for (node in element.childNodes.asList()) {
                                traverseNodesAndRender(node)
                            }
                        }
                    }
                    // [Link](https://loritta.website/)
                    "A" -> {
                        A(href = element.getAttribute("href")) {
                            for (node in element.childNodes.asList()) {
                                traverseNodesAndRender(node)
                            }
                        }
                    }
                    else -> {
                        // Unknown tag name, just loop thru the child nodes
                        for (node in element.childNodes.asList()) {
                            traverseNodesAndRender(node)
                        }
                    }
                }
            }
        }
    }

    traverseNodesAndRender(parsedToHTML.documentElement as HTMLElement)
}