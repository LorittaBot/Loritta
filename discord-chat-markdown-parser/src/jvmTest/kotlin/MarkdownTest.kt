
import net.perfectdreams.loritta.discordchatmarkdownparser.*
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownTest {
    val parser = DiscordChatMarkdownParser()

    @Test
    fun `test strikethrough`() {
        val node = parser.parse("~~strike!~~")

        validateTree(
            node,
            0,
            listOf(
                ExpectedEntry(
                    0,
                    StrikethroughNode::class
                ),
                ExpectedEntry(
                    0,
                    DiscordTextNode::class
                ),
                ExpectedEntry(
                    0,
                    TextNode::class
                ) {
                    it as TextNode
                    assertEquals("strike!", it.text)
                }
            )
        )
    }

    @Test
    fun `test bold`() {
        val node = parser.parse("**bold!**")

        validateTree(
            node,
            0,
            listOf(
                ExpectedEntry(
                    0,
                    BoldNode::class
                ),
                ExpectedEntry(
                    0,
                    DiscordTextNode::class
                ),
                ExpectedEntry(
                    0,
                    TextNode::class
                ) {
                    it as TextNode
                    assertEquals("bold!", it.text)
                }
            )
        )
    }

    @Test
    fun `test codeblock`() {
        val node = parser.parse("""```
            |**this should not be bold**
            |```
        """.trimMargin())

        validateTree(
            node,
            0,
            listOf(
                ExpectedEntry(
                    0,
                    CodeBlockNode::class
                ) {
                    it as CodeBlockNode
                    assertEquals(it.language, null)
                },
                ExpectedEntry(
                    0,
                    CodeTextNode::class
                ) {
                    it as CodeTextNode
                    assertEquals("**this should not be bold**\n", it.text)
                },
            )
        )
    }

    @Test
    fun `test codeblock with language`() {
        val node = parser.parse("""```kotlin
            |println("hello world!")
            |```
        """.trimMargin())

        validateTree(
            node,
            0,
            listOf(
                ExpectedEntry(
                    0,
                    CodeBlockNode::class
                ) {
                    it as CodeBlockNode
                    assertEquals(it.language, "kotlin")
                },
                ExpectedEntry(
                    0,
                    CodeTextNode::class
                ) {
                    it as CodeTextNode
                    assertEquals("println(\"hello world!\")\n", it.text)
                },
            )
        )
    }

    private fun validateTree(
        node: MarkdownNode,
        currentIndex: Int,
        expectedTree: List<ExpectedEntry>
    ) {
        if (node is CompositeMarkdownNode) {
            val expectedEntry = expectedTree[currentIndex]
            val nodeByIndex = node.children[expectedEntry.childrenIndex]

            assertEquals(expectedEntry.expectedClazz, nodeByIndex::class)

            expectedEntry.postExpected.invoke(nodeByIndex)

            validateTree(nodeByIndex, currentIndex + 1, expectedTree)
        }
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

    private data class ExpectedEntry(
        val childrenIndex: Int,
        val expectedClazz: KClass<*>,
        val postExpected: (MarkdownNode) -> (Unit) = { true }
    )
}