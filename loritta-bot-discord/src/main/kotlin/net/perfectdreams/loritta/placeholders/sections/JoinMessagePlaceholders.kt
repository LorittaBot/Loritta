package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.PlaceholderGroups
import net.perfectdreams.loritta.placeholders.Placeholders

object JoinMessagePlaceholders : SectionPlaceholders<JoinMessagePlaceholders.JoinMessagePlaceholder> {
    sealed class JoinMessagePlaceholder(placeholders: List<LorittaPlaceholder>) : SectionPlaceholder(placeholders)

    data object UserMentionPlaceholder : JoinMessagePlaceholder(PlaceholderGroups.USER_MENTION)
    data object UserNamePlaceholder : JoinMessagePlaceholder(PlaceholderGroups.USER_NAME)
    data object UserDiscriminatorPlaceholder : JoinMessagePlaceholder(PlaceholderGroups.USER_DISCRIMINATOR)
    data object UserTagPlaceholder : JoinMessagePlaceholder(PlaceholderGroups.USER_TAG)
    data object GuildNamePlaceholder : JoinMessagePlaceholder(PlaceholderGroups.GUILD_NAME)
    data object GuildSizePlaceholder : JoinMessagePlaceholder(PlaceholderGroups.GUILD_SIZE)
    data object GuildIconUrlPlaceholder : JoinMessagePlaceholder(PlaceholderGroups.GUILD_ICON_URL)

    override val placeholders = listOf<JoinMessagePlaceholder>(
        UserMentionPlaceholder,
        UserNamePlaceholder,
        UserDiscriminatorPlaceholder,
        UserTagPlaceholder,
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder
    )
}