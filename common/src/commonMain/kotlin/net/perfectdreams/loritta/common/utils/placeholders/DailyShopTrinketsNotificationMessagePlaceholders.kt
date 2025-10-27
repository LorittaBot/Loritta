package net.perfectdreams.loritta.common.utils.placeholders

import net.perfectdreams.loritta.i18n.I18nKeysData

@Deprecated("This should not be used, use the new placeholder system instead.")
data object DailyShopTrinketsNotificationMessagePlaceholders : SectionPlaceholders<DailyShopTrinketsNotificationMessagePlaceholders.DailyShopTrinketsNotificationPlaceholder> {
    override val type = PlaceholderSectionType.DAILY_SHOP_TRINKETS_NOTIFICATION_MESSAGE

    sealed interface DailyShopTrinketsNotificationPlaceholder : MessagePlaceholder

    object DailyShopDateShortPlaceholder : GenericPlaceholders.GenericPlaceholder(null), DailyShopTrinketsNotificationPlaceholder {
        override val names = listOf(Placeholders.DAILY_SHOP_DATE_SHORT.toVisiblePlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }
    object GuildNamePlaceholder : GenericPlaceholders.GuildNamePlaceholder(I18nKeysData.Placeholders.Generic.GuildName), DailyShopTrinketsNotificationPlaceholder
    object GuildSizePlaceholder : GenericPlaceholders.GuildSizePlaceholder(I18nKeysData.Placeholders.Generic.GuildSize), DailyShopTrinketsNotificationPlaceholder
    object GuildIconUrlPlaceholder : GenericPlaceholders.GuildIconUrlPlaceholder(I18nKeysData.Placeholders.Generic.GuildIconUrl), DailyShopTrinketsNotificationPlaceholder

    override val placeholders = listOf<DailyShopTrinketsNotificationPlaceholder>(
        DailyShopDateShortPlaceholder,
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder
    )
}