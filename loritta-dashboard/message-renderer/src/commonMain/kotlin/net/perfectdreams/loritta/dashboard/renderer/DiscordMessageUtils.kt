package net.perfectdreams.loritta.dashboard.renderer

import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup
import kotlin.sequences.forEach

object DiscordMessageUtils {
    val DiscordEmote = Regex("<(a)?:([a-zA-Z0-9_]+):([0-9]+)>")
    val DiscordChannel = Regex("<#([0-9]+)>")
    val DiscordRole = Regex("<@&([0-9]+)>")
    val DiscordEveryone = Regex("@everyone")
    val DiscordHere = Regex("@here")
    // Yes the last \\ IS REQUIRED!! RegEx will complain that raw brackets are not allowed in unicode mode without the escaping!!!
    private val Placeholder = Regex("\\{([A-z0-9@\\-.]+)\\}")

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

        if (DrawableType.DISCORD_ROLE in allowedDrawableTypes) {
            DiscordRole.findAll(text)
                .forEach {
                    matches.add(DiscordRoleRegexMatch(it))
                }

            // TODO: Move this to a separate drawable type
            DiscordEveryone.findAll(text)
                .forEach {
                    matches.add(DiscordRawMentionRegexMatch(it))
                }

            DiscordHere.findAll(text)
                .forEach {
                    matches.add(DiscordRawMentionRegexMatch(it))
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

                is DiscordRoleRegexMatch -> {
                    val roleId = matchResult.groupValues[1]
                    sections.add(DrawableDiscordRole(roleId.toLong()))
                }

                is DiscordRawMentionRegexMatch -> {
                    sections.add(DrawableDiscordRawMention(matchResult.groupValues[0]))
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
    fun parsePlaceholdersToString(input: String, placeholderGroups: List<MessageEditorMessagePlaceholderGroup>): String {
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
                        var matchedPlaceholderGroup: MessageEditorMessagePlaceholderGroup? = null

                        for (placeholderGroup in placeholderGroups) {
                            if (placeholderGroup.placeholders.any { it.name == section.placeholderName }) {
                                matchedPlaceholderGroup = placeholderGroup
                                break
                            }
                        }

                        if (matchedPlaceholderGroup != null) {
                            append(matchedPlaceholderGroup.replaceWithFrontend)
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
    private class DiscordRoleRegexMatch(match: MatchResult) : RegexMatch(match)
    private class DiscordRawMentionRegexMatch(match: MatchResult) : RegexMatch(match)

    sealed class DrawableSection
    data class DrawableText(val text: String) : DrawableSection()
    data class DrawablePlaceholder(val placeholderName: String) : DrawableSection()
    data class DrawableDiscordEmote(val emoteId: Long, val animated: Boolean) : DrawableSection()
    data class DrawableDiscordChannel(val channelId: Long) : DrawableSection()
    data class DrawableDiscordRole(val roleId: Long) : DrawableSection()
    data class DrawableDiscordRawMention(val text: String) : DrawableSection()

    enum class DrawableType {
        TEXT,
        PLACEHOLDER,
        DISCORD_EMOJI,
        DISCORD_CHANNEL,
        DISCORD_ROLE
    }
}