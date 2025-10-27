package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.PlaceholderGroups
import net.perfectdreams.loritta.placeholders.Placeholders

object BlueskyPostPlaceholders : SectionPlaceholders<BlueskyPostPlaceholders.BlueskyPostPlaceholder> {
    sealed class BlueskyPostPlaceholder(placeholders: List<LorittaPlaceholder>) : SectionPlaceholder(placeholders)

    data object GuildNamePlaceholder : BlueskyPostPlaceholder(PlaceholderGroups.GUILD_NAME)
    data object GuildSizePlaceholder : BlueskyPostPlaceholder(PlaceholderGroups.GUILD_SIZE)
    data object GuildIconUrlPlaceholder : BlueskyPostPlaceholder(PlaceholderGroups.GUILD_ICON_URL)

    data object PostUrlPlaceholder : BlueskyPostPlaceholder(listOf(Placeholders.BLUESKY_POST_URL))

    override val placeholders = listOf<BlueskyPostPlaceholder>(
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder,
        PostUrlPlaceholder
    )
}