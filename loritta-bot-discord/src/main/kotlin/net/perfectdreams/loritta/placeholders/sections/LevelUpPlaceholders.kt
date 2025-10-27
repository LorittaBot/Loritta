package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.PlaceholderGroups
import net.perfectdreams.loritta.placeholders.Placeholders

object LevelUpPlaceholders : SectionPlaceholders<LevelUpPlaceholders.LevelUpPlaceholder> {
    sealed class LevelUpPlaceholder(placeholders: List<LorittaPlaceholder>) : SectionPlaceholder(placeholders)

    data object UserMentionPlaceholder : LevelUpPlaceholder(PlaceholderGroups.USER_MENTION)
    data object UserNamePlaceholder : LevelUpPlaceholder(PlaceholderGroups.USER_NAME)
    data object UserDiscriminatorPlaceholder : LevelUpPlaceholder(PlaceholderGroups.USER_DISCRIMINATOR)
    data object UserTagPlaceholder : LevelUpPlaceholder(PlaceholderGroups.USER_TAG)
    data object GuildNamePlaceholder : LevelUpPlaceholder(PlaceholderGroups.GUILD_NAME)
    data object GuildSizePlaceholder : LevelUpPlaceholder(PlaceholderGroups.GUILD_SIZE)
    data object GuildIconUrlPlaceholder : LevelUpPlaceholder(PlaceholderGroups.GUILD_ICON_URL)

    data object LevelUpLevelPlaceholder : LevelUpPlaceholder(listOf(Placeholders.EXPERIENCE_LEVEL, Placeholders.EXPERIENCE_LEVEL_SHORT))
    data object LevelUpXPPlaceholder : LevelUpPlaceholder(listOf(Placeholders.EXPERIENCE_XP, Placeholders.EXPERIENCE_XP_SHORT))
    data object LevelUpNextLevelPlaceholder : LevelUpPlaceholder(listOf(Placeholders.EXPERIENCE_NEXT_LEVEL))
    data object LevelUpRankingPlaceholder : LevelUpPlaceholder(listOf(Placeholders.EXPERIENCE_RANKING))
    data object LevelUpNextLevelRequiredXPPlaceholder : LevelUpPlaceholder(listOf(Placeholders.EXPERIENCE_NEXT_LEVEL_REQUIRED_XP))
    data object LevelUpNextLevelTotalXPPlaceholder : LevelUpPlaceholder(listOf(Placeholders.EXPERIENCE_NEXT_LEVEL_TOTAL_XP))
    data object LevelUpNextLevelRoleRewardPlaceholder : LevelUpPlaceholder(listOf(Placeholders.EXPERIENCE_NEXT_ROLE_REWARD))

    override val placeholders = listOf<LevelUpPlaceholder>(
        UserMentionPlaceholder,
        UserNamePlaceholder,
        UserDiscriminatorPlaceholder,
        UserTagPlaceholder,
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder,

        LevelUpLevelPlaceholder,
        LevelUpXPPlaceholder,
        LevelUpNextLevelPlaceholder,
        LevelUpRankingPlaceholder,
        LevelUpNextLevelRequiredXPPlaceholder,
        LevelUpNextLevelTotalXPPlaceholder,
        LevelUpNextLevelRoleRewardPlaceholder,
    )
}