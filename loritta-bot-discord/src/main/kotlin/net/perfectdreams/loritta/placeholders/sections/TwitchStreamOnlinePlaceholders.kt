package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.PlaceholderGroups
import net.perfectdreams.loritta.placeholders.Placeholders

object TwitchStreamOnlinePlaceholders : SectionPlaceholders<TwitchStreamOnlinePlaceholders.TwitchStreamOnlinePlaceholder> {
    sealed class TwitchStreamOnlinePlaceholder(placeholders: List<LorittaPlaceholder>) : SectionPlaceholder(placeholders)

    data object GuildNamePlaceholder : TwitchStreamOnlinePlaceholder(PlaceholderGroups.GUILD_NAME)
    data object GuildSizePlaceholder : TwitchStreamOnlinePlaceholder(PlaceholderGroups.GUILD_SIZE)
    data object GuildIconUrlPlaceholder : TwitchStreamOnlinePlaceholder(PlaceholderGroups.GUILD_ICON_URL)

    data object StreamTitlePlaceholder : TwitchStreamOnlinePlaceholder(listOf(Placeholders.STREAM_TITLE))
    data object StreamGamePlaceholder : TwitchStreamOnlinePlaceholder(listOf(Placeholders.STREAM_GAME))
    data object StreamUrlPlaceholder : TwitchStreamOnlinePlaceholder(listOf(Placeholders.STREAM_URL))

    override val placeholders = listOf<TwitchStreamOnlinePlaceholder>(
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder,
        StreamTitlePlaceholder,
        StreamGamePlaceholder,
        StreamUrlPlaceholder
    )
}