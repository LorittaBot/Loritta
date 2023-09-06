package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

object DiscordMessageUtils {
    val showdown = ShowdownConverter().apply {
        setOption("simpleLineBreaks", true)
        setOption("strikethrough", true)
    }

    private val DiscordEmote = Regex("<(a)?:([a-zA-Z0-9_]+):([0-9]+)>")
    // Yes the last \\ IS REQUIRED!! RegEx will complain that raw brackets are not allowed in unicode mode without the escaping!!!
    private val Placeholder = Regex("\\{([A-z0-9@\\-]+)\\}")

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
    fun parsePlaceholdersToString(input: String, placeholders: List<MessagePlaceholder>): String {
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
                        val placeholder = placeholders.firstOrNull { it.name == section.placeholderName }

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

    sealed class DrawableSection
    data class DrawableText(val text: String) : DrawableSection()
    data class DrawablePlaceholder(val placeholderName: String) : DrawableSection()
    data class DrawableDiscordEmote(val emoteId: Long, val animated: Boolean) : DrawableSection()

    enum class DrawableType {
        TEXT,
        PLACEHOLDER,
        DISCORD_EMOJI
    }
}