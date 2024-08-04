
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

    @Test
    fun `test jvm stackoverflow`() {
        // This should not throw
        val nodes = parser.parse("""
```
1. Carl-bot        10.5M   [+10,000]
2. Midjourney Bot  9.6M    [+8,500]
3. Jockie Music    4.8M    [+5,502 | +0.11%]
4. Rythm           15.1M   [+5,000]
5. Dyno            9.6M    [+3,957 | +0.04%]
6. Tupperbox       4.0M    [+3,500]
7. Ticket Tool     3.5M    [+3,106 | +0.09%]
8. OwO             4.0M    [+3,000]
9. Mimu            2.3M    [+2,408 | +0.10%]
10. FlaviBot        453.1K  [+2,022 | +0.45%]
11. Arcane          1.9M    [+2,004 | +0.11%]
12. Tickets         1.2M    [+2,000]
13. DISBOARD        2.6M    [+1,500]
14. Xenon           2.4M    [+1,500]
15. Loritta         3.2M    [+1,325 | +0.04%]
16. Sapphire        455.8K  [+1,300 | +0.29%]
17. Koya            3.0M    [+1,183 | +0.04%]
18. ServerStats     3.2M    [+1,111 | +0.03%]
19. GiselleBot      24.9K   [+1,029 | +4.30%]
20. Lawliet         1.7M    [+1,017 | +0.06%]
21. Security        755.0K  [+1,001 | +0.13%]
22. Confessions     571.0K  [+1,000 | +0.18%]
23. Akinator        147.0K  [+1,000 | +0.68%]
24. Invite Tracker  1.4M    [+977 | +0.07%]
25. Yggdrasil       2.5M    [+970 | +0.04%]
26. TTS Bot         397.0K  [+903 | +0.23%]
27. Maki            1.1M    [+881 | +0.07%]
28. Bloxlink        1.1M    [+856 | +0.07%]
29. FredBoat        7.5M    [+845 | +0.01%]
30. PluralKit       793.0K  [+800]
31. fmbot           488.1K  [+791 | +0.16%]
32. UnbelievaBoat   2.3M    [+683 | +0.03%]
33. Appy            286.6K  [+682 | +0.24%]
34. Welcomer        582.0K  [+669 | +0.12%]
35. DraftBot        692.7K  [+663 | +0.10%]
36. Giveaway Boat   245.0K  [+659 | +0.27%]
37. JuniperBot      1.1M    [+624 | +0.06%]
38. counting        855.8K  [+587 | +0.07%]
39. Truth or Dare   1.5M    [+558 | +0.04%]
40. Pokecord        605.0K  [+550]
41. Green-bot       1.4M    [+507 | +0.04%]
42. YouTube         557.5K  [+497 | +0.09%]
43.                 80.3K   [+472 | +0.59%]
44. Streamcord      1.1M    [+421 | +0.04%]
45. Wick            643.7K  [+403 | +0.06%]
46. Pancake         3.5M    [+396 | +0.01%]
47. JukeBox         17.4K   [+379 | +2.23%]
48. Euphony         139.1K  [+353 | +0.25%]
49. FreeStuff       593.0K  [+350]
50. Autorole        157.9K  [+350 | +0.22%]
51. Ticket King     132.5K  [+329 | +0.25%]
52. NotifyMe        44.8K   [+296 | +0.66%]
53. Poke-Name       222.3K  [+294 | +0.13%]
54. Assyst          135.3K  [+294 | +0.22%]
55. Simsek          48.1K   [+294 | +0.61%]
56. Marpel          238.0K  [+281 | +0.12%]
57. SOFI            192.0K  [+279 | +0.15%]
58. Uzox            425.1K  [+276 | +0.06%]
59. Craig           363.0K  [+266 | +0.07%]
60. Pokemon         123.4K  [+262 | +0.21%]
61. NQN             1.1M    [+258 | +0.02%]
62. PokeMeow        493.5K  [+256 | +0.05%]
63. TempVoice       120.8K  [+250 | +0.21%]
64. Color-Chan      667.1K  [+249 | +0.04%]
65. ValoTracker  Valorant Stats34.5K   [+245 | +0.71%]
66. Birthday Bot    491.7K  [+244 | +0.05%]
67. Would You       21.1K   [+237 | +1.13%]
68. AI Image Generator179.1K  [+231 | +0.13%]
69. iTranslator     51.8K   [+222 | +0.43%]
70. StickyBot       129.8K  [+215 | +0.17%]
71. JukeDisc        57.1K   [+214 | +0.38%]
72. VerifyME        36.4K   [+202 | +0.56%]
73. PatchBot        392.5K  [+188 | +0.05%]
74. Verifier        127.1K  [+186 | +0.15%]
75. Circle          139.4K  [+184 | +0.13%]
76. Minerea         177.7K  [+169 | +0.10%]
77. Dice Roller     74.6K   [+169 | +0.23%]
78. SlashBot        118.7K  [+131 | +0.11%]
79. Garam           82.9K   [+131 | +0.16%]
80. Karuta          837.8K  [+130 | +0.02%]
81. Starboard       109.7K  [+125 | +0.11%]
82. QOTD Bot        134.8K  [+121 | +0.09%]
83. ChronicleBot    31.1K   [+112 | +0.36%]
84. NukeBot         30.8K   [+110 | +0.36%]
85. Gusic           79.4K   [+108 | +0.14%]
86. Captchabot      492.7K  [+107 | +0.02%]
87. Tickety         41.7K   [+104 | +0.25%]
88. Eevee           30.0K   [+103 | +0.34%]
89. Gachapon        21.3K   [+101 | +0.48%]
90. Kabals          104.6K  [+99 | +0.09%]
91. ClearChat Bot   17.4K   [+96 | +0.55%]
```
    """.trimMargin())
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