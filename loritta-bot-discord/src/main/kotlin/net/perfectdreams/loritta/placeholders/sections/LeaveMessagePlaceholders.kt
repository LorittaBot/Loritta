package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.PlaceholderGroups

object LeaveMessagePlaceholders : SectionPlaceholders<LeaveMessagePlaceholders.LeaveMessagePlaceholder> {
    sealed class LeaveMessagePlaceholder(placeholders: List<LorittaPlaceholder>) : SectionPlaceholder(placeholders)

    data object UserMentionPlaceholder : LeaveMessagePlaceholder(PlaceholderGroups.USER_MENTION)
    data object UserNamePlaceholder : LeaveMessagePlaceholder(PlaceholderGroups.USER_NAME)
    data object UserDiscriminatorPlaceholder : LeaveMessagePlaceholder(PlaceholderGroups.USER_DISCRIMINATOR)
    data object UserTagPlaceholder : LeaveMessagePlaceholder(PlaceholderGroups.USER_TAG)
    data object GuildNamePlaceholder : LeaveMessagePlaceholder(PlaceholderGroups.GUILD_NAME)
    data object GuildSizePlaceholder : LeaveMessagePlaceholder(PlaceholderGroups.GUILD_SIZE)
    data object GuildIconUrlPlaceholder : LeaveMessagePlaceholder(PlaceholderGroups.GUILD_ICON_URL)

    override val placeholders = listOf<LeaveMessagePlaceholder>(
        UserMentionPlaceholder,
        UserNamePlaceholder,
        UserDiscriminatorPlaceholder,
        UserTagPlaceholder,
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder
    )
}