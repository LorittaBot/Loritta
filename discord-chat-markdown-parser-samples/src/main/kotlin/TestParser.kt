import net.perfectdreams.loritta.discordchatmarkdownparser.*

fun main() {
    val parser = DiscordChatMarkdownParser()

    /* val nodes = parser.parse("""usa o "Salvar Mensagem" da Tester em alguma mensagem daqui
        |
        |owo -# this is a subtext `inline code yeah`
        |
        |> blockquote text
        |not inside the blockquote!
    """.trimMargin()) */
    // val nodes = parser.parse("> block quote\nnot in a block quote\n\nyeah <:lori_zz:964701978091470919>".trimMargin())
    val nodes = parser.parse("***bold italics***")
    /* val nodes = parser.parse("""# hello world!!!
        |how are you today?
    """.trimMargin()) */

    // val nodes = parser.parse("hello world")

    println(nodes)

    println("RECURSIVE PRINT:")
    recursivePrint(0, nodes)
}

fun recursivePrint(level: Int, node: MarkdownNode) {
    when (node) {
        is CompositeMarkdownNode -> {
            println("----".repeat(level) + "Composite Node $node")
            when (node) {
                is ChatRootNode -> {
                    for (children in node.children) {
                        recursivePrint(1, children)
                    }
                }
                is BoldNode -> {
                    for (children in node.children) {
                        recursivePrint(level + 1, children)
                    }
                }
                is ItalicsNode -> {
                    for (children in node.children) {
                        recursivePrint(level + 1, children)
                    }
                }
                is StrikethroughNode -> {
                    for (children in node.children) {
                        recursivePrint(level + 1, children)
                    }
                }
                is CodeBlockNode -> {
                    for (children in node.children) {
                        recursivePrint(level + 1, children)
                    }
                }
                is InlineCodeNode -> {
                    for (children in node.children) {
                        recursivePrint(level + 1, children)
                    }
                }
                is HeaderNode -> {
                    for (children in node.children) {
                        recursivePrint(level + 1, children)
                    }
                }
                is BlockQuoteNode -> {
                    for (children in node.children) {
                        recursivePrint(level + 1, children)
                    }
                }
                is DiscordTextNode -> {
                    for (children in node.children) {
                        recursivePrint(level + 1, children)
                    }
                }
                is SubTextNode -> {
                    for (children in node.children) {
                        recursivePrint(level + 1, children)
                    }
                }
                is MaskedLinkNode -> {
                    for (children in node.children) {
                        recursivePrint(level + 1, children)
                    }
                }
            }
        }
        is LeafMarkdownNode -> {
            println("----".repeat(level) + "Leaf Node $node")
            when (node) {
                is TextNode -> {
                    println("----".repeat(level) + node.text.replace("\n", "*new line*"))
                }

                is LinkNode -> {
                    println("----".repeat(level) + " url! " + node.url)
                }

                is CodeTextNode -> {
                    println("----".repeat(level) + " code! " + node.text.replace("\n", "*new line*"))
                }

                is DiscordEmojiEntityNode -> {
                    println("----".repeat(level) + " emoji! " + node.id)
                }

                is DiscordEveryoneMentionEntityNode -> {
                    println("----".repeat(level) + " everyone! ")
                }

                is DiscordHereMentionEntityNode -> {
                    println("----".repeat(level) + " here! ")
                }

                is DiscordCommandEntityNode -> {
                    println("----".repeat(level) + " command! " + node.id)
                }

                is DiscordUserMentionEntityNode -> {
                    println("----".repeat(level) + " user! " + node.id)
                }
            }
        }
    }
}