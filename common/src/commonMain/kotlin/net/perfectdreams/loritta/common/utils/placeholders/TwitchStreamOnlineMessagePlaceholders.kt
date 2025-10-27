package net.perfectdreams.loritta.common.utils.placeholders

import net.perfectdreams.loritta.i18n.I18nKeysData

@Deprecated("This should not be used, use the new placeholder system instead.")
data object TwitchStreamOnlineMessagePlaceholders : SectionPlaceholders<TwitchStreamOnlineMessagePlaceholders.TwitchStreamOnlineMessagePlaceholder> {
    override val type = PlaceholderSectionType.TWITCH_STREAM_ONLINE_MESSAGE

    sealed interface TwitchStreamOnlineMessagePlaceholder : MessagePlaceholder

    object StreamTitlePlaceholder : GenericPlaceholders.GenericPlaceholder(null), TwitchStreamOnlineMessagePlaceholder {
        override val names = listOf(Placeholders.STREAM_TITLE.toVisiblePlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }
    object StreamGamePlaceholder : GenericPlaceholders.GenericPlaceholder(null), TwitchStreamOnlineMessagePlaceholder {
        override val names = listOf(Placeholders.STREAM_GAME.toVisiblePlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }
    object StreamUrlPlaceholder : GenericPlaceholders.GenericPlaceholder(null), TwitchStreamOnlineMessagePlaceholder {
        override val names = listOf(Placeholders.STREAM_URL.toVisiblePlaceholder(), Placeholders.LINK.toHiddenPlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }
    object GuildNamePlaceholder : GenericPlaceholders.GuildNamePlaceholder(I18nKeysData.Placeholders.Generic.GuildName), TwitchStreamOnlineMessagePlaceholder
    object GuildSizePlaceholder : GenericPlaceholders.GuildSizePlaceholder(I18nKeysData.Placeholders.Generic.GuildSize), TwitchStreamOnlineMessagePlaceholder
    object GuildIconUrlPlaceholder : GenericPlaceholders.GuildIconUrlPlaceholder(I18nKeysData.Placeholders.Generic.GuildIconUrl), TwitchStreamOnlineMessagePlaceholder

    override val placeholders = listOf<TwitchStreamOnlineMessagePlaceholder>(
        StreamUrlPlaceholder,
        StreamTitlePlaceholder,
        StreamGamePlaceholder,
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder
    )
}