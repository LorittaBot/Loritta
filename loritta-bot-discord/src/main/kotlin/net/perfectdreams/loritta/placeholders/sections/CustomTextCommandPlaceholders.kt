package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.PlaceholderGroups
import net.perfectdreams.loritta.placeholders.sections.BlockedCommandChannelPlaceholders.BlockedCommandChannelPlaceholder

object CustomTextCommandPlaceholders : SectionPlaceholders<CustomTextCommandPlaceholders.CustomTextCommandPlaceholder> {
    sealed class CustomTextCommandPlaceholder(placeholders: List<LorittaPlaceholder>) : SectionPlaceholder(placeholders)

    data object UserMentionPlaceholder : CustomTextCommandPlaceholder(PlaceholderGroups.USER_MENTION)
    data object UserNamePlaceholder : CustomTextCommandPlaceholder(PlaceholderGroups.USER_NAME)
    data object UserAvatarUrlPlaceholder : CustomTextCommandPlaceholder(PlaceholderGroups.USER_AVATAR_URL)
    data object UserIdPlaceholder : CustomTextCommandPlaceholder(PlaceholderGroups.USER_ID)
    data object UserDiscriminatorPlaceholder : CustomTextCommandPlaceholder(PlaceholderGroups.USER_DISCRIMINATOR)
    data object UserTagPlaceholder : CustomTextCommandPlaceholder(PlaceholderGroups.USER_TAG)
    data object GuildNamePlaceholder : CustomTextCommandPlaceholder(PlaceholderGroups.GUILD_NAME)
    data object GuildSizePlaceholder : CustomTextCommandPlaceholder(PlaceholderGroups.GUILD_SIZE)
    data object GuildIconUrlPlaceholder : CustomTextCommandPlaceholder(PlaceholderGroups.GUILD_ICON_URL)

    override val placeholders = listOf<CustomTextCommandPlaceholder>(
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