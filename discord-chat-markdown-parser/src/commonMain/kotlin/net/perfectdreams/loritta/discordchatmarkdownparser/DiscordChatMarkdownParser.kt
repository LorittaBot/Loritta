package net.perfectdreams.loritta.discordchatmarkdownparser

class DiscordChatMarkdownParser {
    companion object {
        private val EMPTY_SPACE_REGEX = Regex(" ")
        private val DISCORD_MARKDOWN_REGEX = Regex("(\\*\\*(?<bold>(?:.|\\n)+?)\\*\\*|\\*(?<italics>(?:.|\\n)+?)\\*|~~(?<strikethrough>(?:.|\\n)+?)~~|```(?:(?<codeblocklanguage>[A-z0-9]+)\\n|\\n?)?(?<codeblock>(?:.|\\n)+?)```|^-# (?<subtext>.+)?|^(?<header>#{1,3} .+)\n?|`(?<inlinecode>(?:.|\\n)+?)`|^> (?<blockquote>.+)?|\\[(?<discordlinklabel>.+)\\]\\((?<discordlinkurl>https?:\\/\\/.+)\\)|(?<discordurl>https?://[A-z0-9./?=\\-&]+))", RegexOption.MULTILINE)
        private val DISCORD_ENTITIES_REGEX = Regex("(<(?<discordemojianimated>a)?:(?<discordemojiname>[a-zA-Z0-9_]+):(?<discordemojiid>[0-9]+)>|</(?<discordcommandpath>[-_\\p{L}\\p{N}\\p{sc=Deva}\\p{sc=Thai} ]+):(?<discordcommandid>[0-9]+)>|<@!?(?<discorduserid>[0-9]+)>)|(?<discordeveryone>@everyone)|(?<discordhere>@here)|<#(?<discordchannelid>[0-9]+)>|<@&(?<discordroleid>[0-9]+)>", RegexOption.MULTILINE)
    }

    fun parse(markdownText: String): MarkdownNode {
        return ChatRootNode(parseInner(markdownText))
    }

    private fun parseInner(markdownText: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()

        // println("Parsing markdown: $markdownText")

        // We use RegEx for this...
        // I thought about using state machines, but isn't RegEx just a fancy abstraction for a state machine? xd
        val messageSections = getMessageSections(DISCORD_MARKDOWN_REGEX, markdownText)

        for (messageSection in messageSections) {
            when (messageSection) {
                is MessageSection.RegExMatchedSection -> {
                    val groups = messageSection.matchResult.groups

                    val bold = groups["bold"]
                    if (bold != null) {
                        nodes.add(BoldNode(parseInner(bold.value)))
                        continue
                    }

                    val italics = groups["italics"]
                    if (italics != null) {
                        nodes.add(ItalicsNode(parseInner(italics.value)))
                        continue
                    }

                    val strikethrough = groups["strikethrough"]
                    if (strikethrough != null) {
                        nodes.add(StrikethroughNode(parseInner(strikethrough.value)))
                        continue
                    }

                    val codeblock = groups["codeblock"]
                    if (codeblock != null) {
                        val codeblockLanguage = groups["codeblocklanguage"]
                        // Code block nodes should be parsed as is, don't attempt to parse what's inside
                        nodes.add(CodeBlockNode(codeblockLanguage?.value, listOf(CodeTextNode(codeblock.value))))
                        continue
                    }

                    val header = groups["header"]
                    if (header != null) {
                        // TODO: Create a empty regex later
                        val (headerLevelHashtags, headerText) = header.value.split(EMPTY_SPACE_REGEX, 2)
                        nodes.add(HeaderNode(headerLevelHashtags.length, parseInner(headerText)))
                        continue
                    }

                    val subTextNode = groups["subtext"]
                    if (subTextNode != null) {
                        nodes.add(SubTextNode(parseInner(subTextNode.value)))
                        continue
                    }

                    val inlinecode = groups["inlinecode"]
                    if (inlinecode != null) {
                        // Code block nodes should be parsed as is, don't attempt to parse what's inside
                        nodes.add(InlineCodeNode(listOf(CodeTextNode(inlinecode.value))))
                        continue
                    }

                    val blockquote = groups["blockquote"]
                    if (blockquote != null) {
                        nodes.add(BlockQuoteNode(parseInner(blockquote.value)))
                        continue
                    }

                    val discordlinklabel = groups["discordlinklabel"]
                    val discordlinkurl = groups["discordlinkurl"]
                    if (discordlinklabel != null && discordlinkurl != null) {
                        nodes.add(MaskedLinkNode(discordlinkurl.value, parseInner(discordlinklabel.value)))
                        continue
                    }

                    val discordurl = groups["discordurl"]
                    if (discordurl != null) {
                        nodes.add(LinkNode(discordurl.value))
                        continue
                    }
                }
                is MessageSection.TextSection -> {
                    // When parsing text sections, we will do all the section dance all over again, this time to parse Discord entities
                    val entityMessageSections = getMessageSections(DISCORD_ENTITIES_REGEX, messageSection.text)

                    val entityNodes = mutableListOf<LeafMarkdownNode>()

                    for (entityMessageSection in entityMessageSections) {
                        when (entityMessageSection) {
                            is MessageSection.RegExMatchedSection -> {
                                val groups = entityMessageSection.matchResult.groups

                                val discordemojianimated = groups["discordemojianimated"]
                                val discordemojiname = groups["discordemojiname"]
                                val discordemojiid = groups["discordemojiid"]

                                if (discordemojiname != null && discordemojiid != null) {
                                    entityNodes.add(
                                        DiscordEmojiEntityNode(
                                            discordemojiid.value.toLong(),
                                            discordemojiname.value,
                                            discordemojianimated?.value == "a",
                                        )
                                    )
                                    continue
                                }

                                val discordcommandid = groups["discordcommandid"]
                                val discordcommandpath = groups["discordcommandpath"]

                                if (discordcommandid != null && discordcommandpath != null) {
                                    entityNodes.add(
                                        DiscordCommandEntityNode(
                                            discordcommandid.value.toLong(),
                                            discordcommandpath.value
                                        )
                                    )
                                    continue
                                }

                                val discorduserid = groups["discorduserid"]
                                if (discorduserid != null) {
                                    entityNodes.add(
                                        DiscordUserMentionEntityNode(
                                            discorduserid.value.toLong()
                                        )
                                    )
                                    continue
                                }

                                val discordchannelid = groups["discordchannelid"]
                                if (discordchannelid != null) {
                                    entityNodes.add(
                                        DiscordChannelMentionEntityNode(
                                            discordchannelid.value.toLong()
                                        )
                                    )
                                    continue
                                }

                                val discordroleid = groups["discordroleid"]
                                if (discordroleid != null) {
                                    entityNodes.add(
                                        DiscordRoleMentionEntityNode(
                                            discordroleid.value.toLong()
                                        )
                                    )
                                    continue
                                }

                                val discordeveryone = groups["discordeveryone"]
                                if (discordeveryone != null) {
                                    entityNodes.add(DiscordEveryoneMentionEntityNode)
                                    continue
                                }

                                val discordhere = groups["discordhere"]
                                if (discordhere != null) {
                                    entityNodes.add(DiscordHereMentionEntityNode)
                                    continue
                                }
                            }

                            is MessageSection.TextSection -> {
                                val originalText = entityMessageSection.text

                                // We need to fancy parse the text here, to avoid the "getMessageSections" function stripping new lines multiple times
                                // We remove the \n ONLY at the beginning to avoid unnecessary new lines, we don't need to strip new lines at the end of the string
                                val newText = originalText

                                if (newText.isNotEmpty()) {
                                    entityNodes.add(TextNode(newText))
                                }
                            }
                        }
                    }

                    nodes.add(DiscordTextNode(entityNodes))
                }
            }
        }

        return nodes
    }

    private fun getMessageSections(regex: Regex, markdownText: String): List<MessageSection> {
        // This is a bit hard because the RegEx does NOT include a "fallback", to avoid issues with greedy matching too much, and lazy matching only single characters
        // So we are going to do this differently...
        val foundMatches = regex.findAll(markdownText)
            .toList()

        val messageSections = mutableListOf<MessageSection>()

        var index = 0
        val buffer = StringBuilder()

        fun addTextSection() {
            if (buffer.isNotEmpty()) {
                messageSections.add(MessageSection.TextSection(buffer.toString()))
            }
            buffer.clear()
        }

        while (true) {
            if (index >= markdownText.length)
                break

            val matchThatMatchesThisIndex = foundMatches.firstOrNull { index in it.range }
            if (matchThatMatchesThisIndex != null) {
                addTextSection()

                messageSections.add(MessageSection.RegExMatchedSection(matchThatMatchesThisIndex))
                index = matchThatMatchesThisIndex.range.last
            } else {
                buffer.append(markdownText[index])
            }

            index++
        }

        addTextSection()

        // println("Sections: ${messageSections}")

        return messageSections
    }

    private sealed class MessageSection {
        class RegExMatchedSection(val matchResult: MatchResult) : MessageSection()
        class TextSection(val text: String) : MessageSection()
    }
}