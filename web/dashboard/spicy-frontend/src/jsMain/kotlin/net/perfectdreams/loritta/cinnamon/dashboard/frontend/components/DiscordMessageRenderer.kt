package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import js.core.jso
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.DiscordMessageUtils
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.RenderableMessagePlaceholder
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.DiscordCdn
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.Image
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.parse
import net.perfectdreams.loritta.common.utils.Color
import net.perfectdreams.loritta.common.utils.embeds.DiscordComponent
import net.perfectdreams.loritta.common.utils.embeds.DiscordEmbed
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.common.utils.placeholders.MessagePlaceholder
import net.perfectdreams.loritta.serializable.DiscordChannel
import net.perfectdreams.loritta.serializable.DiscordRole
import net.perfectdreams.loritta.serializable.DiscordUser
import org.jetbrains.compose.web.attributes.href
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.asList
import org.w3c.dom.parsing.DOMParser

/**
 * Renders a Discord message
 */
@Composable
fun DiscordMessageRenderer(
    author: RenderableDiscordUser,
    message: DiscordMessage,
    additionalMessageData: AdditionalMessageData?,
    channels: List<DiscordChannel>,
    roles: List<DiscordRole>,
    placeholders: List<RenderableMessagePlaceholder>
) {
    DiscordMessageStyle {
        DiscordMessageBlock(author.name, author.avatarUrl, author.bot) {
            // ===[ MESSAGE CONTENT ]===
            Div {
                TransformedDiscordText(message.content, channels, roles, placeholders)
            }

            val embed = message.embed
            DiscordMessageAccessories {
                val attachments = additionalMessageData?.attachments
                if (attachments != null)
                    DiscordMessageAttachments(attachments.map { it.url })

                // ===[ EMBED ]===
                if (embed != null) {
                    DiscordMessageEmbed(
                        embed.color,
                        embed.thumbnail?.url?.let {
                            DiscordMessageUtils.parsePlaceholdersToString(
                                it,
                                placeholders
                            )
                        }
                    ) {
                        // ===[ EMBED AUTHOR ]===
                        val embedAuthor = embed.author
                        if (embedAuthor != null) {
                            DiscordAuthor(
                                embedAuthor.url?.let { DiscordMessageUtils.parsePlaceholdersToString(it, placeholders) },
                                embedAuthor.iconUrl?.let { DiscordMessageUtils.parsePlaceholdersToString(it, placeholders) }
                            ) {
                                Text(
                                    DiscordMessageUtils.parsePlaceholdersToString(
                                        embedAuthor.name,
                                        placeholders
                                    )
                                )
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
                                    TransformedDiscordText(title, channels, roles, placeholders)
                                }
                            } else {
                                Div(attrs = {
                                    classes("discord-embed-title")
                                }) {
                                    TransformedDiscordText(title, channels, roles, placeholders)
                                }
                            }
                        }

                        // ===[ EMBED DESCRIPTION ]===
                        val description = embed.description
                        if (description != null)
                            DiscordEmbedDescription {
                                TransformedDiscordText(description, channels, roles, placeholders)
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
                                                    "style",
                                                    if (!field.inline) "grid-column: 1 / 13;" else {
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
                                                    TransformedDiscordText(field.name, channels, roles, placeholders)
                                                }

                                                Div(attrs = {
                                                    classes("discord-embed-field-value")
                                                }) {
                                                    TransformedDiscordText(field.value, channels, roles, placeholders)
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
                        if (imageUrl != null)
                            DiscordEmbedImage(imageUrl)

                        val footer = embed.footer
                        if (footer != null) {
                            Div(attrs = {
                                classes("discord-embed-footer")
                            }) {
                                val footerIconUrl = footer.iconUrl
                                if (footerIconUrl != null) {
                                    Img(
                                        src = DiscordMessageUtils.parsePlaceholdersToString(
                                            footerIconUrl,
                                            placeholders
                                        )
                                    ) {
                                        classes("discord-embed-footer-icon")
                                    }
                                }

                                Span(attrs = {
                                    classes("discord-embed-footer-text")
                                }) {
                                    Text(
                                        DiscordMessageUtils.parsePlaceholdersToString(
                                            footer.text,
                                            placeholders
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                val components = message.components
                if (components != null) {
                    Div(attrs = {
                        classes("discord-components")
                    }) {
                        for (component in components) {
                            DiscordComponent(component)
                        }
                    }
                }

                val reactions = additionalMessageData?.reactions
                if (reactions != null) {
                    Div(attrs = {
                        classes("discord-message-reactions")
                    }) {
                        reactions.forEach {
                            Div(attrs = {
                                classes("discord-message-reaction")
                            }) {
                                UIIcon(SVGIconManager.star)
                                Text(" ")
                                Text(it.count.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscordMessageStyle(content: ContentBuilder<HTMLDivElement>) = Div(
    attrs = {
        classes("discord-style")
    },
    content = content
)

@Composable
fun DiscordMessageSidebar(content: ContentBuilder<HTMLDivElement>) = Div(
    attrs = {
        classes("discord-message-sidebar")
    },
    content = content
)

@Composable
fun DiscordMessageBlock(username: String, avatarUrl: String, isBot: Boolean, content: ContentBuilder<HTMLDivElement>) = Div(
    attrs = {
        classes("discord-message")
    }
) {
    DiscordMessageSidebar {
        Img(src = avatarUrl) {
            classes("discord-message-avatar")
        }
    }

    Div(
        attrs = {
            classes("discord-message-content")
        },
        content = {
            H2(attrs = {
                classes("discord-message-header")
            }) {
                Span(attrs = {
                    classes("discord-message-username")
                    attr("style", "color: rgb(233, 30, 99);")
                }) {
                    Text(username)
                }
                if (isBot) {
                    Span(attrs = {
                        classes("discord-message-bot-tag")
                    }) {
                        Text("BOT")
                    }
                }
                Span(attrs = {
                    classes("discord-message-timestamp")
                }) {
                    Text("Today at 09:07")
                }
            }

            content()
        }
    )
}

@Composable
fun DiscordMessageAccessories(content: ContentBuilder<HTMLDivElement>) = Div(
    attrs = {
        classes("discord-message-accessories")
    }
) {
    content()
}

@Composable
fun DiscordMessageAttachments(attachments: List<String>) = Div(attrs = {
    classes("discord-message-attachments")
}) {
    attachments.forEach {
        Img(src = it) {
            classes("discord-message-attachment")
        }
    }
}

@Composable
fun DiscordMessageReactions(content: ContentBuilder<HTMLElement>) = Div(attrs = {
    classes("discord-message-reactions")
}) {
    content()
}

@Composable
fun DiscordMessageReaction(content: ContentBuilder<HTMLElement>) = Div(attrs = {
    classes("discord-message-reaction")
    attr("style", "display: flex; align-items: center; justify-content: center; gap: 0.5em;")
}) {
    content()
}

@Composable
fun DiscordComponents(content: ContentBuilder<HTMLElement>) = Div(attrs = {
    classes("discord-components")
}) {
    content()
}

@Composable
fun DiscordActionRow(content: ContentBuilder<HTMLElement>) = Div(attrs = {
    classes("discord-action-row")
}) {
    content()
}

@Composable
fun DiscordMessageEmbed(
    color: Int?,
    thumbnailUrl: String?,
    content: ContentBuilder<HTMLElement>,
) {
    Article(attrs = {
        classes("discord-embed")

        // ===[ EMBED PILL ]===
        val embedColor = color
        if (embedColor != null) {
            // Extract the red, green, and blue components
            val red = embedColor shr 16 and 0xFF
            val green = embedColor shr 8 and 0xFF
            val blue = embedColor and 0xFF

            val hexRed = red.toString(16).padStart(2, '0')
            val hexGreen = green.toString(16).padStart(2, '0')
            val hexBlue = blue.toString(16).padStart(2, '0')

            attr("style", "border-color: #$hexRed$hexGreen$hexBlue;")
        }
    }) {
        Div(attrs = {
            classes("discord-embed-content")
        }) {
            content()
        }

        // ===[ EMBED THUMBNAIL ]===
        // This stays outside of the embed content
        if (thumbnailUrl != null) {
            A(attrs = {
                classes("discord-embed-thumbnail")
            }) {
                Img(
                    src = thumbnailUrl
                ) {
                    attr("style", "width: 100%;")
                }
            }
        }
    }
}

@Composable
fun DiscordEmbedDescription(content: ContentBuilder<HTMLDivElement>) {
    Div(attrs = {
        classes("discord-embed-description")
    }) {
        content()
    }
}

@Composable
fun DiscordEmbedImage(imageUrl: String) {
    Div(attrs = {
        classes("discord-embed-image")
    }) {
        Img(
            src = imageUrl
        ) {
            attr("style", "width: 100%;")
        }
    }
}

@Composable
fun DiscordAuthor(
    authorUrl: String?,
    authorIconUrl: String?,
    content: ContentBuilder<HTMLElement>,
) {
    Div(attrs = {
        classes("discord-embed-author")
    }) {
        if (authorIconUrl != null) {
            Img(
                src = authorIconUrl,
                attrs = {
                    classes("discord-embed-icon")
                })
        }

        if (authorUrl != null) {
            A(
                href = authorUrl,
                attrs = {
                    classes("discord-embed-text")
                }
            ) {
                content()
            }
        } else {
            Span(attrs = {
                classes("discord-embed-text")
            }) {
                content()
            }
        }
    }
}

@Composable
fun DiscordComponent(component: DiscordComponent) {
    when (component) {
        is DiscordComponent.DiscordActionRow -> DiscordActionRow(component)
        is DiscordComponent.DiscordButton -> DiscordLinkButton(component)
    }
}

@Composable
fun DiscordActionRow(component: DiscordComponent.DiscordActionRow) {
    Div(attrs = {
        classes("discord-action-row")
    }) {
        for (component in component.components) {
            DiscordComponent(component)
        }
    }
}

@Composable
private fun DiscordLinkButton(component: DiscordComponent.DiscordButton) = DiscordLinkButton {
    Text(component.label)
}

@Composable
fun DiscordLinkButton(content: ContentBuilder<HTMLElement>) {
    DiscordButton(DiscordButtonType.SECONDARY) {
        Div({
            classes("text-with-icon-wrapper")
        }) {
            Div {
                content()
            }

            UIIcon(SVGIconManager.arrowUpRightFromSquare) {
                attr("style", "width: 1em; height: 1em;")
                classes("button-icon")
            }
        }
    }
}

@Composable
private fun TransformedDiscordText(
    input: String,
    channels: List<DiscordChannel>,
    roles: List<DiscordRole>,
    placeholders: List<RenderableMessagePlaceholder>,
) {
    // We DON'T strip the HTML, because that would trigger issues with animated emojis getting stripped out
    // But don't worry, we don't *really* need it, why?
    // Because we traverse all nodes manually and check if they should be appended to the element or not, so injection is impossible
    // (Well, technically you CAN inject it, like, you can use <a href="google.com">hello</a>, but would that *really* be bad? We are
    // filtering the data anyway)

    // Now we will convert some of Discord's entities into HTML tags, to avoid them being detected as HTML tags when rendering the nodes
    // (such as animated emotes, due to <a:emote_name... being detected as a anchor link)
    val maskedInput = DiscordMessageUtils.convertSpecialDiscordEntitiesIntoHTMLTags(input)

    val convertedFromMarkdown = parse(
        DiscordMessageUtils.patchMultiNewLines(DiscordMessageUtils.patchBlockQuotes(maskedInput)),
        jso {
            // We need to keep breaks as false because we use our own patch new lines function
            this.breaks = false
        }
    )

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
                        is DiscordMessageUtils.DrawableDiscordEmote -> error("Drawable Discord Emote found, but this shouldn't happen!")
                        is DiscordMessageUtils.DrawableDiscordRole -> {
                            val role = roles.firstOrNull { it.id == section.roleId }
                            if (role != null) {
                                val color = if (role.color != 0x1FFFFFFF) Color(role.color) else null

                                InlineDiscordMention("@${role.name}", color)
                            } else {
                                InlineDiscordMention("@Cargo Desconhecido", Color(237, 66, 69))
                            }
                        }
                        is DiscordMessageUtils.DrawableDiscordChannel -> {
                            val channel = channels.firstOrNull { it.id == section.channelId }
                            if (channel != null) {
                                InlineDiscordMention("#${channel.name}")
                            } else {
                                InlineDiscordMention("#Canal Desconhecido", Color(237, 66, 69))
                            }
                        }
                        is DiscordMessageUtils.DrawableText -> Text(section.text)
                        is DiscordMessageUtils.DrawablePlaceholder -> {
                            val placeholder = placeholders.firstOrNull { it.placeholder.names.any { it.placeholder.name == section.placeholderName }}

                            // TODO: Add a hover tooltip when you hover a placeholder, to show what placeholder triggers it
                            if (placeholder != null) {
                                when (placeholder.placeholder.renderType) {
                                    // TODO: Convert text with URL
                                    MessagePlaceholder.RenderType.TEXT -> Text(placeholder.replaceWith)
                                    MessagePlaceholder.RenderType.MENTION -> InlineDiscordMention(placeholder.replaceWith)
                                }
                            } else {
                                // Unknown placeholder!
                                InlineDiscordMention("Placeholder inválido!", Color(237, 66, 69))
                            }
                        }
                        is DiscordMessageUtils.DrawableDiscordRawMention -> {
                            InlineDiscordMention(section.text) // The "section.text" already includes the @
                        }
                    }
                }
            }
            Node.ELEMENT_NODE -> {
                val tagName = (element as? HTMLElement)?.tagName
                println(tagName)

                when (tagName) {
                    // Custom tag
                    "DISCORD-EMOJI" -> {
                        val animated = element.getAttribute("animated").toBoolean()
                        val emoteName = element.getAttribute("name")
                        val emoteId = element.getAttribute("id")

                        println("emoteId: $emoteId")
                        println("emoteName: $emoteName")
                        println("animated? $animated")

                        val extension = if (animated)
                            "gif"
                        else
                            "png"
                        Img(src = "https://cdn.discordapp.com/emojis/$emoteId.$extension?v=1", attrs = {
                            classes("discord-inline-emoji")
                        })
                    }
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
                    // ~~Strikethrough~~
                    "DEL" -> {
                        Span(attrs = {
                            attr("style", "text-decoration: line-through;")
                        }) {
                            for (node in element.childNodes.asList()) {
                                traverseNodesAndRender(node)
                            }
                        }
                    }
                    // Line break (brazil lmao)
                    "BR" -> {
                        Br {}
                    }
                    // Paragraph
                    "P" -> {
                        P {
                            for (node in element.childNodes.asList()) {
                                traverseNodesAndRender(node)
                            }
                        }
                    }
                    // # Header 1
                    "H1" -> {
                        H1 {
                            for (node in element.childNodes.asList()) {
                                traverseNodesAndRender(node)
                            }
                        }
                    }
                    // # Header 2
                    "H2" -> {
                        H2 {
                            for (node in element.childNodes.asList()) {
                                traverseNodesAndRender(node)
                            }
                        }
                    }
                    // # Header 3
                    "H3" -> {
                        H3 {
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
                    // > quote
                    "BLOCKQUOTE" -> {
                        Div(attrs = {
                            attr("style", "display: flex; gap: 0.5em;")
                        }) {
                            Div(attrs = {
                                attr("style", "width: 4px; border-radius: 4px; background-color: #c4c9ce;")
                            })

                            Div {
                                for (node in element.childNodes.asList()) {
                                    traverseNodesAndRender(node)
                                }
                            }
                        }
                    }
                    // Lists
                    "LI" -> {
                        Li {
                            for (node in element.childNodes.asList()) {
                                traverseNodesAndRender(node)
                            }
                        }
                    }
                    // Lists
                    "UL" -> {
                        Ul {
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

data class RenderableDiscordUser(
    val name: String,
    val avatarUrl: String,
    val bot: Boolean
) {
    companion object {
        fun fromDiscordUser(user: DiscordUser): RenderableDiscordUser {
            val avatarId = user.avatarId
            val url = if (avatarId != null) {
                DiscordCdn.userAvatar(user.id.toULong(), avatarId)
                    .toUrl()
            } else {
                DiscordCdn.defaultAvatarLegacy(0)
                    .toUrl {
                        format = Image.Format.PNG // For some weird reason, the default avatars aren't available in webp format (why?)
                    }
            }

            return RenderableDiscordUser(
                user.globalName ?: user.name,
                url,
                true
            )
        }
    }
}

/**
 * Additional message data for the render
 */
data class AdditionalMessageData(
    val reactions: List<Reaction>,
    val attachments: List<Attachment>
) {
    data class Reaction(
        val count: Int
    )

    data class Attachment(
        val url: String
    )
}