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
                    println("----".repeat(level) + "Code: ${node.language}")

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

                is DiscordRoleMentionEntityNode -> {
                    println("----".repeat(level) + " role! " + node.id)
                }

                is DiscordChannelMentionEntityNode -> {
                    println("----".repeat(level) + " channel! " + node.id)
                }
            }
        }
    }
}