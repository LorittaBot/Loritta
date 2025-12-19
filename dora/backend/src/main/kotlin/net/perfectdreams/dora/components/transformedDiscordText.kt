package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.br
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.i
import kotlinx.html.img
import kotlinx.html.pre
import kotlinx.html.span
import kotlinx.html.style
import net.perfectdreams.loritta.discordchatmarkdownparser.BlockQuoteNode
import net.perfectdreams.loritta.discordchatmarkdownparser.BoldNode
import net.perfectdreams.loritta.discordchatmarkdownparser.ChatRootNode
import net.perfectdreams.loritta.discordchatmarkdownparser.CodeBlockNode
import net.perfectdreams.loritta.discordchatmarkdownparser.CodeTextNode
import net.perfectdreams.loritta.discordchatmarkdownparser.CompositeMarkdownNode
import net.perfectdreams.loritta.discordchatmarkdownparser.DiscordChannelMentionEntityNode
import net.perfectdreams.loritta.discordchatmarkdownparser.DiscordChatMarkdownParser
import net.perfectdreams.loritta.discordchatmarkdownparser.DiscordCommandEntityNode
import net.perfectdreams.loritta.discordchatmarkdownparser.DiscordEmojiEntityNode
import net.perfectdreams.loritta.discordchatmarkdownparser.DiscordEveryoneMentionEntityNode
import net.perfectdreams.loritta.discordchatmarkdownparser.DiscordHereMentionEntityNode
import net.perfectdreams.loritta.discordchatmarkdownparser.DiscordRoleMentionEntityNode
import net.perfectdreams.loritta.discordchatmarkdownparser.DiscordUserMentionEntityNode
import net.perfectdreams.loritta.discordchatmarkdownparser.HeaderNode
import net.perfectdreams.loritta.discordchatmarkdownparser.InlineCodeNode
import net.perfectdreams.loritta.discordchatmarkdownparser.ItalicsNode
import net.perfectdreams.loritta.discordchatmarkdownparser.LeafMarkdownNode
import net.perfectdreams.loritta.discordchatmarkdownparser.LinkNode
import net.perfectdreams.loritta.discordchatmarkdownparser.MarkdownNode
import net.perfectdreams.loritta.discordchatmarkdownparser.MaskedLinkNode
import net.perfectdreams.loritta.discordchatmarkdownparser.StrikethroughNode
import net.perfectdreams.loritta.discordchatmarkdownparser.SubTextNode
import net.perfectdreams.loritta.discordchatmarkdownparser.TextNode
import kotlin.text.StringBuilder

private val parser = DiscordChatMarkdownParser()

fun FlowContent.transformedDiscordText(input: String) {
    val convertedFromMarkdown = parser.parse(input) as ChatRootNode // Should ALWAYS be a chat root node!

    fun renderBufferedText(text: String) {
        val buffer = StringBuilder()

        fun FlowContent.renderNodesAndClearBuffer() {
            val result = buffer.toString()
            text(result)
            buffer.clear()
        }

        for (character in text) {
            if (character == '\n') {
                renderNodesAndClearBuffer()
                br {}
            } else {
                buffer.append(character.toString())
            }
        }

        renderNodesAndClearBuffer()
    }

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
                        renderBufferedText(element.text)
                    }

                    is LinkNode -> {
                        a(href = "#") { // No need to point it to the real URL
                            text(element.url)
                        }
                    }

                    is CodeTextNode -> {
                        renderBufferedText(element.text)
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

                        span(classes = "discord-mention") {
                            // val color = Color(mentionedRoleColor)
                            // style = "color: rgb(${color.red}, ${color.green}, ${color.blue});"

                            text("@$roleId")
                        }
                    }

                    is DiscordChannelMentionEntityNode -> {
                        val channelId = element.id

                        span(classes = "discord-mention") {
                            // TODO: Fix channel icon!
                            text("#$channelId")
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

                    is CodeTextNode -> {
                        TODO()
                    }
                    is DiscordChannelMentionEntityNode -> {
                        TODO()
                    }
                    is DiscordCommandEntityNode -> {
                        TODO()
                    }
                    is DiscordEmojiEntityNode -> {
                        TODO()
                    }
                    DiscordEveryoneMentionEntityNode -> {
                        TODO()
                    }
                    DiscordHereMentionEntityNode -> {
                        TODO()
                    }
                    is DiscordRoleMentionEntityNode -> {
                        TODO()
                    }
                    is DiscordUserMentionEntityNode -> {
                        TODO()
                    }
                }
            }
        }
    }

    traverseNodesAndRender(convertedFromMarkdown)
}