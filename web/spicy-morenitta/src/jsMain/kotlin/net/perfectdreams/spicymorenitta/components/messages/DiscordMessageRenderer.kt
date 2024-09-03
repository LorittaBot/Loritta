package net.perfectdreams.spicymorenitta.components.messages

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.common.utils.Color
import net.perfectdreams.loritta.common.utils.embeds.DiscordComponent
import net.perfectdreams.loritta.common.utils.embeds.DiscordEmbed
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.common.utils.placeholders.MessagePlaceholder
import net.perfectdreams.loritta.discordchatmarkdownparser.*
import net.perfectdreams.loritta.serializable.DiscordChannel
import net.perfectdreams.loritta.serializable.DiscordRole
import net.perfectdreams.loritta.serializable.DiscordUser
import net.perfectdreams.loritta.serializable.messageeditor.MessageEditorMessagePlaceholder
import org.jetbrains.compose.web.attributes.href
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

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
    placeholders: List<MessageEditorMessagePlaceholder>
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
                                // TODO: Fix this
                                // UIIcon(SVGIconManager.star)
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
                        Text("APP")
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

            // TODO: Fix this!
            /* UIIcon(SVGIconManager.arrowUpRightFromSquare) {
                attr("style", "width: 1em; height: 1em;")
                classes("button-icon")
            } */
        }
    }
}

private val parser = DiscordChatMarkdownParser()

@Composable
private fun TransformedDiscordText(
    input: String,
    channels: List<DiscordChannel>,
    roles: List<DiscordRole>,
    placeholders: List<MessageEditorMessagePlaceholder>
) {
    val convertedFromMarkdown = parser.parse(input) as ChatRootNode // Should ALWAYS be a chat root node!

    // Based on the original DiscordMessageRenderer used for Loritta's "Save Message" feature
    @Composable
    fun traverseNodesAndRender(element: MarkdownNode) {
        when (element) {
            is CompositeMarkdownNode -> {
                when (element) {
                    is BoldNode -> {
                        B {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is ItalicsNode -> {
                        I {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is CodeBlockNode -> {
                        Pre {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is HeaderNode -> {
                        when (element.level) {
                            1 -> {
                                H1 {
                                    for (children in element.children) {
                                        traverseNodesAndRender(children)
                                    }
                                }
                            }

                            2 -> {
                                H2 {
                                    for (children in element.children) {
                                        traverseNodesAndRender(children)
                                    }
                                }
                            }

                            3 -> {
                                H3 {
                                    for (children in element.children) {
                                        traverseNodesAndRender(children)
                                    }
                                }
                            }

                            else -> error("Unsupported header level ${element.level}")
                        }
                    }

                    is InlineCodeNode -> {
                        Code {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is StrikethroughNode -> {
                        Span(attrs = {
                            attr("style", "text-decoration: line-through;")
                        }) {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is BlockQuoteNode -> {
                        Div(
                            attrs = {
                                attr("style", "display: flex; gap: 0.5em;")
                            }
                        ) {
                            Div(
                                attrs = {
                                    attr("style", "width: 4px; border-radius: 4px; background-color: #4e5058;")
                                }
                            )

                            Div {
                                for (children in element.children) {
                                    traverseNodesAndRender(children)
                                }
                            }
                        }
                    }

                    is SubTextNode -> {
                        Div {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is MaskedLinkNode -> {
                        // No need to point it to the real URL
                        A(href = "#") {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    else -> {
                        // Unknown/Unparsed node, just loop thru the child nodes
                        for (children in element.children) {
                            traverseNodesAndRender(children)
                        }
                    }
                }
            }

            is LeafMarkdownNode -> {
                when (element) {
                    is TextNode -> {
                        val buffer = StringBuilder()

                        @Composable
                        fun renderNodesAndClearBuffer() {
                            val result = buffer.toString()
                            val nodes = parseStringToNodes(result)
                            for (node in nodes) {
                                when (node) {
                                    is MessagePlaceholderNode -> {
                                        val placeholder = placeholders.firstOrNull { node.placeholder == it.name }

                                        // TODO: Add a hover tooltip when you hover a placeholder, to show what placeholder triggers it
                                        if (placeholder != null) {
                                            when (placeholder.renderType) {
                                                // TODO: Convert text with URL
                                                MessagePlaceholder.RenderType.TEXT -> Text(placeholder.replaceWith)
                                                MessagePlaceholder.RenderType.MENTION -> InlineDiscordMention(placeholder.replaceWith)
                                            }
                                        } else {
                                            // Unknown placeholder!
                                            InlineDiscordMention("Placeholder inválido!", Color(237, 66, 69))
                                        }
                                    }
                                    is MessageTextNode -> Text(node.text)
                                }
                            }
                            buffer.clear()
                        }

                        for (character in element.text) {
                            if (character == '\n') {
                                renderNodesAndClearBuffer()
                                Br {}
                            } else {
                                buffer.append(character.toString())
                            }
                        }

                        renderNodesAndClearBuffer()
                    }

                    is LinkNode -> {
                        A(href = "#") { // No need to point it to the real URL
                            Text(element.url)
                        }
                    }

                    is CodeTextNode -> {
                        Text(element.text)
                    }

                    is DiscordEmojiEntityNode -> {
                        val animated = element.animated
                        val emoteName = element.name
                        val emoteId = element.id

                        val extension = if (animated)
                            "gif"
                        else
                            "png"

                        Img(
                            src = "https://cdn.discordapp.com/emojis/$emoteId.$extension?v=1",
                        ) {
                            classes("discord-inline-emoji")
                        }
                    }

                    is DiscordCommandEntityNode -> {
                        val id = element.id
                        val path = element.path

                        Span(attrs = {
                            classes("discord-mention")
                        }) {
                            Text("/")
                            Text(path)
                        }
                    }

                    is DiscordUserMentionEntityNode -> {
                        val userId = element.id

                        Span(attrs = {
                            classes("discord-mention")
                        }) {
                            Text("@$userId")
                        }
                    }

                    is DiscordRoleMentionEntityNode -> {
                        val roleId = element.id

                        val mentionedRole = roles.firstOrNull { it.id == roleId }
                        val mentionedRoleName = mentionedRole?.name ?: "???"
                        val mentionedRoleColor = mentionedRole?.color

                        Span(attrs = {
                            classes("discord-mention")
                            if (mentionedRoleColor != null) {
                                val color = Color(mentionedRoleColor)
                                attr("style", "color: rgb(${color.red}, ${color.green}, ${color.blue});")
                            }
                        }) {
                            Text("@$mentionedRoleName ($roleId)")
                        }
                    }

                    is DiscordChannelMentionEntityNode -> {
                        val channelId = element.id

                        Span(attrs = {
                            classes("discord-mention")
                        }) {
                            // TODO: Fix channel icon!
                            val mentionedChannel = channels.firstOrNull { it.id == channelId }
                            Text("#${mentionedChannel?.name ?: "???"} ($channelId)")
                        }
                    }

                    is DiscordEveryoneMentionEntityNode -> {
                        Span(attrs = {
                            classes("discord-mention")
                        }) {
                            Text("@everyone")
                        }
                    }

                    is DiscordHereMentionEntityNode -> {
                        Span(attrs = {
                            classes("discord-mention")
                        }) {
                            Text("@here")
                        }
                    }
                }
            }
        }
    }

    traverseNodesAndRender(convertedFromMarkdown)
}

data class RenderableDiscordUser(
    val name: String,
    val avatarUrl: String,
    val bot: Boolean
) {
    companion object {
        fun fromDiscordUser(user: DiscordUser): RenderableDiscordUser {
            // TODO - htmx-mix: Refactor this!
            val avatarId = user.avatarId
            val url = if (avatarId != null) {
                "https://cdn.discordapp.com/avatars/${user.id}/${avatarId}.png"
            } else {
                "https://cdn.discordapp.com/embed/avatars/${(user.id shr 22) % 6}.png"
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

// Define the Node classes
sealed class MessageNode

data class MessageTextNode(val text: String) : MessageNode()
data class MessagePlaceholderNode(val placeholder: String) : MessageNode()

// Function to split the string into nodes
private fun parseStringToNodes(input: String): List<MessageNode> {
    val nodes = mutableListOf<MessageNode>()
    // YES THE \\ IS NEEDED ON THE END OF THE } TO AVOID "raw bracket is not allowed in regular expression with unicode flag"
    val regex = "\\{([@A-z0-9.]+)\\}".toRegex()
    var lastIndex = 0

    regex.findAll(input).forEach { matchResult ->
        val range = matchResult.range

        // Add TextNode for text before the placeholder
        if (lastIndex < range.first) {
            nodes.add(MessageTextNode(input.substring(lastIndex, range.first)))
        }

        // Add PlaceholderNode for the placeholder
        nodes.add(MessagePlaceholderNode(matchResult.groupValues[1]))

        lastIndex = range.last + 1
    }

    // Add any remaining text as a TextNode
    if (lastIndex < input.length) {
        nodes.add(MessageTextNode(input.substring(lastIndex)))
    }

    return nodes
}