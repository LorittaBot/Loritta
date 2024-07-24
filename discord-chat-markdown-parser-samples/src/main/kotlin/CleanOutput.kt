import net.perfectdreams.loritta.discordchatmarkdownparser.*

fun main() {
    val parser = DiscordChatMarkdownParser()
    val node = parser.parse("hello **world**!")

    val builder = StringBuilder()
    recursiveCleanPrint(node, builder)

    println(builder.toString())
}

fun recursiveCleanPrint(node: MarkdownNode, builder: StringBuilder) {
    when (node) {
        is CompositeMarkdownNode -> {
            for (children in node.children) {
                recursiveCleanPrint(children, builder)
            }
        }
        is LeafMarkdownNode -> {
            when (node) {
                is TextNode -> {
                    builder.append(node.text)
                }

                else -> {}
            }
        }
    }
}