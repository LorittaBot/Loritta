import net.perfectdreams.loritta.discordchatmarkdownparser.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownTest {
    val parser = DiscordChatMarkdownParser()

    @Test
    fun `test strikethrough`() {
        val node = parser.parse("~~strike!~~")

        assertEquals("strike!", (((node as ChatRootNode).children.first() as StrikethroughNode).children.first() as TextNode).text)
    }

    @Test
    fun `test bold`() {
        val node = parser.parse("**bold!**")

        assertEquals("bold!", (((node as ChatRootNode).children.first() as BoldNode).children.first() as TextNode).text)
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