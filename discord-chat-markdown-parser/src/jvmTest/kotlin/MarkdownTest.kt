
import net.perfectdreams.loritta.discordchatmarkdownparser.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownTest {
    val parser = DiscordChatMarkdownParser()

    @Test
    fun `test strikethrough`() {
        val node = parser.parse("~~strike!~~")

        assertEquals("strike!", (getFirstLeafNode(node) as TextNode).text)
    }

    @Test
    fun `test bold`() {
        val node = parser.parse("**bold!**")

        assertEquals("bold!", (getFirstLeafNode(node) as TextNode).text)
    }

    private fun getFirstLeafNode(node: MarkdownNode): LeafMarkdownNode {
        if (node is LeafMarkdownNode)
            return node

        if (node is CompositeMarkdownNode) {
            val foundNode = node.children.firstNotNullOfOrNull { getFirstLeafNode(it) } ?: error("Could not find a children node!")
            return foundNode
        }

        error("Whoops, I don't know how to handle a $node!")
    }

    /* @Test
    fun `test code block`() {
        val node = parser.parse("""
                ```
                **markdown inside a codeblock should NOT be rendered**

                -# ever!
                ```
            """.trimIndent())
    } */
}