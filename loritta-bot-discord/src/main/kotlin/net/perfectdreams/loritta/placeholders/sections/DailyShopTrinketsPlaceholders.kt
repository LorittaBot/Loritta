package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.PlaceholderGroups
import net.perfectdreams.loritta.placeholders.Placeholders

object DailyShopTrinketsPlaceholders : SectionPlaceholders<DailyShopTrinketsPlaceholders.DailyShopTrinketsPlaceholder> {
    sealed class DailyShopTrinketsPlaceholder(placeholders: List<LorittaPlaceholder>) : SectionPlaceholder(placeholders)

    data object GuildNamePlaceholder : DailyShopTrinketsPlaceholder(PlaceholderGroups.GUILD_NAME)
    data object GuildSizePlaceholder : DailyShopTrinketsPlaceholder(PlaceholderGroups.GUILD_SIZE)
    data object GuildIconUrlPlaceholder : DailyShopTrinketsPlaceholder(PlaceholderGroups.GUILD_ICON_URL)

    data object DailyShopDateShortPlaceholder : DailyShopTrinketsPlaceholder(listOf(Placeholders.DAILY_SHOP_DATE_SHORT))

    override val placeholders = listOf<DailyShopTrinketsPlaceholder>(
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder,
        DailyShopDateShortPlaceholder
    )
}