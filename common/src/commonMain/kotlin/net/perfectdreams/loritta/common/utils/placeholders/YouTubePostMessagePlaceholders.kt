package net.perfectdreams.loritta.common.utils.placeholders

import net.perfectdreams.loritta.i18n.I18nKeysData

data object YouTubePostMessagePlaceholders : SectionPlaceholders<YouTubePostMessagePlaceholders.YouTubePostMessagePlaceholder> {
    override val type = PlaceholderSectionType.YOUTUBE_POST_MESSAGE

    sealed interface YouTubePostMessagePlaceholder : MessagePlaceholder

    object VideoTitlePlaceholder : GenericPlaceholders.GenericPlaceholder(null), YouTubePostMessagePlaceholder {
        override val names = listOf(Placeholders.VIDEO_TITLE.toVisiblePlaceholder(), Placeholders.Deprecated.VIDEO_TITLE.toHiddenPlaceholder(), Placeholders.Deprecated.VIDEO_TITLE_BR.toHiddenPlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }
    object VideoIdPlaceholder : GenericPlaceholders.GenericPlaceholder(null), YouTubePostMessagePlaceholder {
        override val names = listOf(Placeholders.VIDEO_ID.toVisiblePlaceholder(), Placeholders.Deprecated.VIDEO_ID.toHiddenPlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }
    object VideoUrlPlaceholder : GenericPlaceholders.GenericPlaceholder(null), YouTubePostMessagePlaceholder {
        override val names = listOf(Placeholders.VIDEO_URL.toVisiblePlaceholder(), Placeholders.Deprecated.VIDEO_URL.toHiddenPlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }
    object VideoThumbnailPlaceholder : GenericPlaceholders.GenericPlaceholder(null), YouTubePostMessagePlaceholder {
        override val names = listOf(Placeholders.VIDEO_THUMBNAIL.toVisiblePlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }
    object GuildNamePlaceholder : GenericPlaceholders.GuildNamePlaceholder(I18nKeysData.Placeholders.Generic.GuildName), YouTubePostMessagePlaceholder
    object GuildSizePlaceholder : GenericPlaceholders.GuildSizePlaceholder(I18nKeysData.Placeholders.Generic.GuildSize), YouTubePostMessagePlaceholder
    object GuildIconUrlPlaceholder : GenericPlaceholders.GuildIconUrlPlaceholder(I18nKeysData.Placeholders.Generic.GuildIconUrl), YouTubePostMessagePlaceholder

    override val placeholders = listOf<YouTubePostMessagePlaceholder>(
        VideoUrlPlaceholder,
        VideoThumbnailPlaceholder,
        VideoTitlePlaceholder,
        VideoIdPlaceholder,
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder
    )
}