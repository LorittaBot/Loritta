package net.perfectdreams.loritta.dashboard.renderer

import kotlinx.html.*
import net.perfectdreams.loritta.dashboard.discord.DiscordChannel
import net.perfectdreams.loritta.dashboard.discord.DiscordRole
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordComponent
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordEmbed
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordMessage
import net.perfectdreams.loritta.dashboard.discordmessages.RenderableDiscordUser
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholder
import net.perfectdreams.loritta.discordchatmarkdownparser.*

/**
 * Renders a Discord message
 */
fun FlowContent.discordMessageRenderer(
    author: RenderableDiscordUser,
    message: DiscordMessage,
    additionalMessageData: AdditionalMessageData?,
    channels: List<DiscordChannel>,
    roles: List<DiscordRole>,
    placeholders: List<MessageEditorMessagePlaceholder>
) {
    discordMessageStyle {
        discordMessageBlock(author.name, author.avatarUrl, author.bot) {
            // ===[ MESSAGE CONTENT ]===
            div {
                transformedDiscordText(message.content, channels, roles, placeholders)
            }

            val embed = message.embed
            discordMessageAccessories {
                val attachments = additionalMessageData?.attachments
                if (attachments != null)
                    discordMessageAttachments(attachments.map { it.url })

                // ===[ EMBED ]===
                if (embed != null) {
                    discordMessageEmbed(
                        embed.color,
                        embed.thumbnail?.url?.let {
                            DiscordMessageUtils.parsePlaceholdersToString(
                                it
                            )
                        }
                    ) {
                        // ===[ EMBED AUTHOR ]===
                        val embedAuthor = embed.author
                        if (embedAuthor != null) {
                            discordAuthor(
                                embedAuthor.url?.let { DiscordMessageUtils.parsePlaceholdersToString(it) },
                                embedAuthor.iconUrl?.let { DiscordMessageUtils.parsePlaceholdersToString(it) }
                            ) {
                                text(
                                    DiscordMessageUtils.parsePlaceholdersToString(
                                        embedAuthor.name
                                    )
                                )
                            }
                        }

                        // ===[ EMBED TITLE ]===
                        val title = embed.title
                        if (title != null) {
                            val titleUrl = embed.url
                            if (titleUrl != null) {
                                a(href = titleUrl, classes = "discord-embed-title") {
                                    transformedDiscordText(title, channels, roles, placeholders)
                                }
                            } else {
                                div(classes = "discord-embed-title") {
                                    transformedDiscordText(title, channels, roles, placeholders)
                                }
                            }
                        }

                        // ===[ EMBED DESCRIPTION ]===
                        val description = embed.description
                        if (description != null)
                            discordEmbedDescription {
                                transformedDiscordText(description, channels, roles, placeholders)
                            }

                        if (embed.fields.isNotEmpty()) {
                            div(classes = "discord-embed-fields") {
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
                                            div(classes = "discord-embed-field") {
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

                                                div(classes = "discord-embed-field-name") {
                                                    transformedDiscordText(field.name, channels, roles, placeholders)
                                                }

                                                div(classes = "discord-embed-field-value") {
                                                    transformedDiscordText(field.value, channels, roles, placeholders)
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
                            discordEmbedImage(imageUrl)

                        val footer = embed.footer
                        if (footer != null) {
                            div(classes = "discord-embed-footer") {
                                val footerIconUrl = footer.iconUrl
                                if (footerIconUrl != null) {
                                    img(
                                        src = DiscordMessageUtils.parsePlaceholdersToString(
                                            footerIconUrl
                                        ),
                                        classes = "discord-embed-footer-icon"
                                    )
                                }

                                span(classes = "discord-embed-footer-text") {
                                    text(
                                        DiscordMessageUtils.parsePlaceholdersToString(
                                            footer.text
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                val components = message.components
                if (components != null) {
                    div(classes = "discord-components") {
                        for (component in components) {
                            discordComponent(component)
                        }
                    }
                }

                val reactions = additionalMessageData?.reactions
                if (reactions != null) {
                    div(classes = "discord-message-reactions") {
                        reactions.forEach {
                            div(classes = "discord-message-reaction") {
                                // TODO: Fix this
                                // UIIcon(SVGIconManager.star)
                                text(" ")
                                text(it.count.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.discordMessageStyle(content: FlowContent.() -> (Unit)) {
    div(classes = "discord-style") {
        content()
    }
}

fun FlowContent.discordMessageSidebar(content: FlowContent.() -> (Unit)) {
    div(classes = "discord-message-sidebar") {
        content()
    }
}

fun FlowContent.discordMessageBlock(username: String, avatarUrl: String, isBot: Boolean, content: FlowContent.() -> (Unit)) {
    div(classes = "discord-message") {
        discordMessageSidebar {
            img(src = avatarUrl, classes = "discord-message-avatar")
        }

        div(classes = "discord-message-content") {
            h2(classes = "discord-message-header") {
                span(classes = "discord-message-username") {
                    style = "color: rgb(233, 30, 99);"

                    text(username)
                }

                if (isBot) {
                    span(classes = "discord-message-bot-tag") {
                        text("APP")
                    }
                }
                span(classes = "discord-message-timestamp") {
                    text("Today at 09:07")
                }
            }

            content()
        }
    }
}

fun FlowContent.discordMessageUserGap() {
    div(classes = "discord-message-user-gap") {}
}

fun FlowContent.discordMessageAccessories(content: FlowContent.() -> Unit) {
    div(classes = "discord-message-accessories") {
        content()
    }
}

fun FlowContent.discordMessageAttachments(attachments: List<String>) {
    div(classes = "discord-message-attachments") {
        attachments.forEach {
            img(src = it) {
                classes = setOf("discord-message-attachment")
            }
        }
    }
}

fun FlowContent.discordMessageReactions(content: FlowContent.() -> Unit) {
    div(classes = "discord-message-reactions") {
        content()
    }
}

fun FlowContent.discordMessageReaction(content: FlowContent.() -> Unit) {
    div(classes = "discord-message-reaction") {
        style = "display: flex; align-items: center; justify-content: center; gap: 0.5em;"
        content()
    }
}

fun FlowContent.discordComponents(content: FlowContent.() -> Unit) {
    div(classes = "discord-components") {
        content()
    }
}

fun FlowContent.discordActionRow(content: FlowContent.() -> Unit) {
    div(classes = "discord-action-row") {
        content()
    }
}

fun FlowContent.discordMessageEmbed(
    color: Int?,
    thumbnailUrl: String?,
    content: FlowContent.() -> Unit,
) {
    article(classes = "discord-embed") {
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

            style = "border-color: #$hexRed$hexGreen$hexBlue;"
        }

        div(classes = "discord-embed-content") {
            content()
        }

        // ===[ EMBED THUMBNAIL ]===
        // This stays outside of the embed content
        if (thumbnailUrl != null) {
            a(classes = "discord-embed-thumbnail") {
                img(src = thumbnailUrl) {
                    style = "width: 100%;"
                }
            }
        }
    }
}

fun FlowContent.discordEmbedDescription(content: FlowContent.() -> Unit) {
    div(classes = "discord-embed-description") {
        content()
    }
}

fun FlowContent.discordEmbedImage(imageUrl: String) {
    div(classes = "discord-embed-image") {
        img(src = imageUrl) {
            style = "width: 100%;"
        }
    }
}

fun FlowContent.discordAuthor(
    authorUrl: String?,
    authorIconUrl: String?,
    content: FlowContent.() -> Unit,
) {
    div(classes = "discord-embed-author") {
        if (authorIconUrl != null) {
            img(src = authorIconUrl) {
                classes = setOf("discord-embed-icon")
            }
        }

        if (authorUrl != null) {
            a(href = authorUrl, classes = "discord-embed-text") {
                content()
            }
        } else {
            span(classes = "discord-embed-text") {
                content()
            }
        }
    }
}

fun FlowContent.discordComponent(component: DiscordComponent) {
    when (component) {
        is DiscordComponent.DiscordActionRow -> discordActionRow(component)
        is DiscordComponent.DiscordButton -> discordLinkButton(component)
    }
}

fun FlowContent.discordActionRow(component: DiscordComponent.DiscordActionRow) {
    div(classes = "discord-action-row") {
        for (componentItem in component.components) {
            discordComponent(componentItem)
        }
    }
}

private fun FlowContent.discordLinkButton(component: DiscordComponent.DiscordButton) = discordLinkButton {
    +component.label
}

fun FlowContent.discordLinkButton(content: FlowContent.() -> Unit) {
    button(classes = "discord-button secondary") {
        div(classes = "text-with-icon-wrapper") {
            div {
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

fun FlowContent.transformedDiscordText(
    input: String,
    channels: List<DiscordChannel>,
    roles: List<DiscordRole>,
    placeholders: List<MessageEditorMessagePlaceholder>
) {
    val convertedFromMarkdown = parser.parse(input) as ChatRootNode // Should ALWAYS be a chat root node!

    // Based on the original DiscordMessageRenderer used for Loritta's "Save Message" feature
    fun FlowContent.traverseNodesAndRender(element: MarkdownNode) {
        when (element) {
            is CompositeMarkdownNode -> {
                when (element) {
                    is BoldNode -> {
                        b {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is ItalicsNode -> {
                        i {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is CodeBlockNode -> {
                        pre {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is HeaderNode -> {
                        when (element.level) {
                            1 -> {
                                h1 {
                                    for (children in element.children) {
                                        traverseNodesAndRender(children)
                                    }
                                }
                            }

                            2 -> {
                                h2 {
                                    for (children in element.children) {
                                        traverseNodesAndRender(children)
                                    }
                                }
                            }

                            3 -> {
                                h3 {
                                    for (children in element.children) {
                                        traverseNodesAndRender(children)
                                    }
                                }
                            }

                            else -> error("Unsupported header level ${element.level}")
                        }
                    }

                    is InlineCodeNode -> {
                        code {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is StrikethroughNode -> {
                        span {
                            style = "text-decoration: line-through;"
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is BlockQuoteNode -> {
                        div {
                            style = "display: flex; gap: 0.5em;"
                            div {
                                style = "width: 4px; border-radius: 4px; background-color: #4e5058;"
                            }

                            div {
                                for (children in element.children) {
                                    traverseNodesAndRender(children)
                                }
                            }
                        }
                    }

                    is SubTextNode -> {
                        div {
                            for (children in element.children) {
                                traverseNodesAndRender(children)
                            }
                        }
                    }

                    is MaskedLinkNode -> {
                        // No need to point it to the real URL
                        a(href = "#") {
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

                        fun FlowContent.renderNodesAndClearBuffer() {
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
                                                MessageEditorMessagePlaceholder.RenderType.TEXT -> text(placeholder.replaceWithFrontend)
                                                MessageEditorMessagePlaceholder.RenderType.MENTION -> {
                                                    span(classes = "discord-mention") {
                                                        text(placeholder.replaceWithFrontend)
                                                    }
                                                }
                                            }
                                        } else {
                                            // Unknown placeholder!
                                            span(classes = "discord-mention") {
                                                style = "--mention-color: rgb(237, 66, 69);"
                                                text("Placeholder inválido!")
                                            }
                                        }
                                    }
                                    is MessageTextNode -> text(node.text)
                                }
                            }
                            buffer.clear()
                        }

                        for (character in element.text) {
                            if (character == '\n') {
                                renderNodesAndClearBuffer()
                                br {}
                            } else {
                                buffer.append(character.toString())
                            }
                        }

                        renderNodesAndClearBuffer()
                    }

                    is LinkNode -> {
                        a(href = "#") { // No need to point it to the real URL
                            text(element.url)
                        }
                    }

                    is CodeTextNode -> {
                        text(element.text)
                    }

                    is DiscordEmojiEntityNode -> {
                        val animated = element.animated
                        val emoteName = element.name
                        val emoteId = element.id

                        val extension = if (animated)
                            "gif"
                        else
                            "png"

                        img(src = "https://cdn.discordapp.com/emojis/$emoteId.$extension?v=1", classes = "discord-inline-emoji")
                    }

                    is DiscordCommandEntityNode -> {
                        val id = element.id
                        val path = element.path

                        span(classes = "discord-mention") {
                            text("/")
                            text(path)
                        }
                    }

                    is DiscordUserMentionEntityNode -> {
                        val userId = element.id

                        span(classes = "discord-mention") {
                            text("@$userId")
                        }
                    }

                    is DiscordRoleMentionEntityNode -> {
                        val roleId = element.id

                        val mentionedRole = roles.firstOrNull { it.id == roleId }
                        val mentionedRoleName = mentionedRole?.name ?: "???"
                        val mentionedRoleColor = mentionedRole?.color

                        span(classes = "discord-mention") {
                            // val color = Color(mentionedRoleColor)
                            // style = "color: rgb(${color.red}, ${color.green}, ${color.blue});"

                            text("@$mentionedRoleName ($roleId)")
                        }
                    }

                    is DiscordChannelMentionEntityNode -> {
                        val channelId = element.id

                        span(classes = "discord-mention") {
                            // TODO: Fix channel icon!
                            val mentionedChannel = channels.firstOrNull { it.id == channelId }
                            text("#${mentionedChannel?.name ?: "???"} ($channelId)")
                        }
                    }

                    is DiscordEveryoneMentionEntityNode -> {
                        span(classes = "discord-mention") {
                            text("@everyone")
                        }
                    }

                    is DiscordHereMentionEntityNode -> {
                        span(classes = "discord-mention") {
                            text("@here")
                        }
                    }

                    is CodeTextNode -> TODO()
                    is DiscordChannelMentionEntityNode -> TODO()
                    is DiscordCommandEntityNode -> TODO()
                    is DiscordEmojiEntityNode -> TODO()
                    DiscordEveryoneMentionEntityNode -> TODO()
                    DiscordHereMentionEntityNode -> TODO()
                    is DiscordRoleMentionEntityNode -> TODO()
                    is DiscordUserMentionEntityNode -> TODO()
                }
            }
        }
    }

    traverseNodesAndRender(convertedFromMarkdown)
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
    val regex = "\\{([@A-z0-9.-]+)\\}".toRegex()
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