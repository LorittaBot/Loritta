package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.PlaceholderGroups
import net.perfectdreams.loritta.placeholders.sections.CustomTextCommandPlaceholders.CustomTextCommandPlaceholder

object InviteBlockedPlaceholders : SectionPlaceholders<InviteBlockedPlaceholders.InviteBlockedPlaceholder> {
    sealed class InviteBlockedPlaceholder(placeholders: List<LorittaPlaceholder>) : SectionPlaceholder(placeholders)

    data object UserMentionPlaceholder : InviteBlockedPlaceholder(PlaceholderGroups.USER_MENTION)
    data object UserNamePlaceholder : InviteBlockedPlaceholder(PlaceholderGroups.USER_NAME)
    data object UserAvatarUrlPlaceholder : InviteBlockedPlaceholder(PlaceholderGroups.USER_AVATAR_URL)
    data object UserIdPlaceholder : InviteBlockedPlaceholder(PlaceholderGroups.USER_ID)
    data object UserDiscriminatorPlaceholder : InviteBlockedPlaceholder(PlaceholderGroups.USER_DISCRIMINATOR)
    data object UserTagPlaceholder : InviteBlockedPlaceholder(PlaceholderGroups.USER_TAG)
    data object GuildNamePlaceholder : InviteBlockedPlaceholder(PlaceholderGroups.GUILD_NAME)
    data object GuildSizePlaceholder : InviteBlockedPlaceholder(PlaceholderGroups.GUILD_SIZE)
    data object GuildIconUrlPlaceholder : InviteBlockedPlaceholder(PlaceholderGroups.GUILD_ICON_URL)

    override val placeholders = listOf<InviteBlockedPlaceholder>(
        UserMentionPlaceholder,
        UserNamePlaceholder,
        UserAvatarUrlPlaceholder,
        UserIdPlaceholder,
        UserDiscriminatorPlaceholder,
        UserTagPlaceholder,
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder
    )
}