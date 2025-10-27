package net.perfectdreams.loritta.common.utils.placeholders

import net.perfectdreams.loritta.i18n.I18nKeysData

@Deprecated("This should not be used, use the new placeholder system instead.")
data object BlueskyPostMessagePlaceholders : SectionPlaceholders<BlueskyPostMessagePlaceholders.BlueskyPostMessagePlaceholder> {
    override val type = PlaceholderSectionType.BLUESKY_POST_MESSAGE

    sealed interface BlueskyPostMessagePlaceholder : MessagePlaceholder

    object PostUrlPlaceholder : GenericPlaceholders.GenericPlaceholder(null), BlueskyPostMessagePlaceholder {
        override val names = listOf(Placeholders.BLUESKY_POST_URL.toVisiblePlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }
    object GuildNamePlaceholder : GenericPlaceholders.GuildNamePlaceholder(I18nKeysData.Placeholders.Generic.GuildName), BlueskyPostMessagePlaceholder
    object GuildSizePlaceholder : GenericPlaceholders.GuildSizePlaceholder(I18nKeysData.Placeholders.Generic.GuildSize), BlueskyPostMessagePlaceholder
    object GuildIconUrlPlaceholder : GenericPlaceholders.GuildIconUrlPlaceholder(I18nKeysData.Placeholders.Generic.GuildIconUrl), BlueskyPostMessagePlaceholder

    override val placeholders = listOf<BlueskyPostMessagePlaceholder>(
        PostUrlPlaceholder,
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder
    )
}