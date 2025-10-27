package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.PlaceholderGroups
import net.perfectdreams.loritta.placeholders.Placeholders
import net.perfectdreams.loritta.placeholders.sections.SectionPlaceholder
import net.perfectdreams.loritta.placeholders.sections.SectionPlaceholders

object YouTubePostPlaceholders : SectionPlaceholders<YouTubePostPlaceholders.YouTubePostPlaceholder> {
    sealed class YouTubePostPlaceholder(placeholders: List<LorittaPlaceholder>) : SectionPlaceholder(placeholders)

    data object GuildNamePlaceholder : YouTubePostPlaceholder(PlaceholderGroups.GUILD_NAME)
    data object GuildSizePlaceholder : YouTubePostPlaceholder(PlaceholderGroups.GUILD_SIZE)
    data object GuildIconUrlPlaceholder : YouTubePostPlaceholder(PlaceholderGroups.GUILD_ICON_URL)

    data object VideoTitlePlaceholder : YouTubePostPlaceholder(listOf(Placeholders.VIDEO_TITLE, Placeholders.Deprecated.VIDEO_TITLE, Placeholders.Deprecated.VIDEO_TITLE_BR))
    data object VideoUrlPlaceholder : YouTubePostPlaceholder(listOf(Placeholders.VIDEO_URL, Placeholders.Deprecated.VIDEO_URL))
    data object VideoIdPlaceholder : YouTubePostPlaceholder(listOf(Placeholders.VIDEO_ID, Placeholders.Deprecated.VIDEO_ID))
    data object VideoThumbnailPlaceholder : YouTubePostPlaceholder(listOf(Placeholders.VIDEO_THUMBNAIL))

    override val placeholders = listOf<YouTubePostPlaceholder>(
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder,
        VideoTitlePlaceholder,
        VideoUrlPlaceholder,
        VideoIdPlaceholder,
        VideoThumbnailPlaceholder,
    )
}