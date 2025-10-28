package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.PlaceholderGroups
import net.perfectdreams.loritta.placeholders.Placeholders
import net.perfectdreams.loritta.placeholders.sections.JoinMessagePlaceholders.JoinMessagePlaceholder

object BlockedCommandChannelPlaceholders : SectionPlaceholders<BlockedCommandChannelPlaceholders.BlockedCommandChannelPlaceholder> {
    sealed class BlockedCommandChannelPlaceholder(placeholders: List<LorittaPlaceholder>) : SectionPlaceholder(placeholders)

    data object UserMentionPlaceholder : BlockedCommandChannelPlaceholder(PlaceholderGroups.USER_MENTION)
    data object UserNamePlaceholder : BlockedCommandChannelPlaceholder(PlaceholderGroups.USER_NAME)
    data object UserAvatarUrlPlaceholder : BlockedCommandChannelPlaceholder(PlaceholderGroups.USER_AVATAR_URL)
    data object UserIdPlaceholder : BlockedCommandChannelPlaceholder(PlaceholderGroups.USER_ID)
    data object UserDiscriminatorPlaceholder : BlockedCommandChannelPlaceholder(PlaceholderGroups.USER_DISCRIMINATOR)
    data object UserTagPlaceholder : BlockedCommandChannelPlaceholder(PlaceholderGroups.USER_TAG)
    data object GuildNamePlaceholder : BlockedCommandChannelPlaceholder(PlaceholderGroups.GUILD_NAME)
    data object GuildSizePlaceholder : BlockedCommandChannelPlaceholder(PlaceholderGroups.GUILD_SIZE)
    data object GuildIconUrlPlaceholder : BlockedCommandChannelPlaceholder(PlaceholderGroups.GUILD_ICON_URL)

    override val placeholders = listOf<BlockedCommandChannelPlaceholder>(
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