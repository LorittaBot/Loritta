package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import kotlinx.browser.document
import kotlinx.dom.createElement

object DiscordMessageUtils {
    val showdown = ShowdownConverter().apply {
        setOption("simpleLineBreaks", true)
        setOption("strikethrough", true)
        setOption("simplifiedAutoLink", true)
    }

    const val LORITTA_MORENITTA_FANCY_NAME = "Loritta Morenitta \uD83D\uDE18"

    val DiscordEmote = Regex("<(a)?:([a-zA-Z0-9_]+):([0-9]+)>")
    val DiscordChannel = Regex("<#([0-9]+)>")
    // Yes the last \\ IS REQUIRED!! RegEx will complain that raw brackets are not allowed in unicode mode without the escaping!!!
    private val Placeholder = Regex("\\{([A-z0-9@\\-.]+)\\}")

    val BlockQuotePatch = Regex("^> .+\\n(?!>)", RegexOption.MULTILINE)
    val MultiNewLinePatch = Regex("^\\n^", RegexOption.MULTILINE)

    /**
     * Patches block quotes to make them act like Discord's block quotes when parsed with Showdown
     *
     * Example:
     * * > Loritta is cute!
     * * This shouldn't be in the block quote
     *
     * Is parsed as:
     * * > Loritta is cute!
     * * > This shouldn't be in the block quote
     *
     * This function adds an extra new line IF the next line isn't a block quote.
     */
    fun patchBlockQuotes(input: String) = BlockQuotePatch.replace(input) {
        "${it.value}\n"
    }

    /**
     * Patches block quotes to make them act like Discord's block quotes when parsed with Showdown
     *
     * Example:
     * * > Loritta is cute!
     * * This shouldn't be in the block quote
     *
     * Is parsed as:
     * * > Loritta is cute!
     * * > This shouldn't be in the block quote
     *
     * This function adds an extra new line IF the next line isn't a block quote.
     */
    fun patchMultiNewLines(input: String) = MultiNewLinePatch.replace(input) {
        "<br />"
    }

    /**
     * Maskes special discord entities (such as emotes) from the input, to avoid them being recognized as HTML entities
     *
     * The input is masked by converting characters to characters in the private use unicode areas, so they SHOULDN'T be used in normal circumstances
     *
     * @see unmaskSpecialDiscordEntities
     */
    fun convertSpecialDiscordEntitiesIntoHTMLTags(
        input: String
    ) = DiscordEmote.replace(input) {
        val animated = it.groupValues[1] == "a"
        val emoteName = it.groupValues[2]
        val emoteId = it.groupValues[3]

        val d = document.createElement("discord-emoji") {
            setAttribute("animated", animated.toString())
            setAttribute("name", emoteName)
            setAttribute("id", emoteId)
        }

        d.outerHTML
    }

    /**
     * Parses the [text] to multiple drawable sections
     */
    fun parseStringToDrawableSections(
        text: String,
        allowedDrawableTypes: List<DrawableType> = DrawableType.values().toList()
    ): MutableList<DrawableSection> {
        val sections = mutableListOf<DrawableSection>()

        val matches = mutableListOf<RegexMatch>()

        if (DrawableType.DISCORD_EMOJI in allowedDrawableTypes) {
            DiscordEmote.findAll(text)
                .forEach {
                    matches.add(DiscordEmoteRegexMatch(it))
                }
        }

        if (DrawableType.DISCORD_CHANNEL in allowedDrawableTypes) {
            DiscordChannel.findAll(text)
                .forEach {
                    matches.add(DiscordChannelRegexMatch(it))
                }
        }

        if (DrawableType.PLACEHOLDER in allowedDrawableTypes) {
            Placeholder.findAll(text)
                .forEach {
                    matches.add(PlaceholderRegexMatch(it))
                }
        }

        var firstMatchedCharacterIndex = 0
        var lastMatchedCharacterIndex = 0

        for (match in matches.sortedBy { it.match.range.first }) {
            val matchResult = match.match
            if (DrawableType.TEXT in allowedDrawableTypes) {
                sections.add(
                    DrawableText(
                        text.substring(
                            firstMatchedCharacterIndex until matchResult.range.first
                        )
                    )
                )
            }

            when (match) {
                is DiscordEmoteRegexMatch -> {
                    val animated = matchResult.groupValues[1] == "a"
                    val emoteName = matchResult.groupValues[2]
                    val emoteId = matchResult.groupValues[3]
                    sections.add(DrawableDiscordEmote(emoteId.toLong(), animated))
                }

                is DiscordChannelRegexMatch -> {
                    val channelId = matchResult.groupValues[1]
                    sections.add(DrawableDiscordChannel(channelId.toLong()))
                }

                is PlaceholderRegexMatch -> {
                    sections.add(DrawablePlaceholder(matchResult.groupValues[1]))
                }
            }

            lastMatchedCharacterIndex = matchResult.range.last + 1
            firstMatchedCharacterIndex = lastMatchedCharacterIndex
        }

        if (DrawableType.TEXT in allowedDrawableTypes) {
            sections.add(
                DrawableText(
                    text.substring(
                        lastMatchedCharacterIndex until text.length
                    )
                )
            )
        }

        return sections
    }

    /**
     * Parses placeholders to a string
     */
    fun parsePlaceholdersToString(input: String, placeholders: List<RenderableMessagePlaceholder>): String {
        val drawableSections = parseStringToDrawableSections(
            input,
            listOf(
                DrawableType.TEXT,
                DrawableType.PLACEHOLDER
            )
        )

        return buildString {
            for (section in drawableSections) {
                when (section) {
                    is DrawablePlaceholder -> {
                        val placeholder = placeholders.firstOrNull { it.placeholder.names.any { it.placeholder.name == section.placeholderName }}

                        if (placeholder != null) {
                            append(placeholder.replaceWith)
                        } else {
                            // Draw placeholder as is if it is an unknown placeholder
                            append("{${section.placeholderName}}")
                        }
                    }
                    is DrawableText -> append(section.text)
                    // This should NEVER happen since we are only requesting TEXT and PLACEHOLDER
                    else -> { error("Unhandled drawable section! ${section::class.simpleName}" )}
                }
            }
        }
    }

    sealed class RegexMatch(val match: MatchResult)
    private class PlaceholderRegexMatch(match: MatchResult) : RegexMatch(match)
    private class DiscordEmoteRegexMatch(match: MatchResult) : RegexMatch(match)
    private class DiscordChannelRegexMatch(match: MatchResult) : RegexMatch(match)

    sealed class DrawableSection
    data class DrawableText(val text: String) : DrawableSection()
    data class DrawablePlaceholder(val placeholderName: String) : DrawableSection()
    data class DrawableDiscordEmote(val emoteId: Long, val animated: Boolean) : DrawableSection()
    data class DrawableDiscordChannel(val channelId: Long) : DrawableSection()

    enum class DrawableType {
        TEXT,
        PLACEHOLDER,
        DISCORD_EMOJI,
        DISCORD_CHANNEL
    }

    enum class RenderDirection {
        VERTICAL,
        HORIZONTAL
    }
}