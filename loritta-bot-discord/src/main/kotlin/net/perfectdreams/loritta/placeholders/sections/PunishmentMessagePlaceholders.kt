package net.perfectdreams.loritta.placeholders.sections

import net.perfectdreams.loritta.placeholders.LorittaPlaceholder
import net.perfectdreams.loritta.placeholders.PlaceholderGroups
import net.perfectdreams.loritta.placeholders.Placeholders
import net.perfectdreams.loritta.placeholders.sections.InviteBlockedPlaceholders.InviteBlockedPlaceholder

object PunishmentMessagePlaceholders : SectionPlaceholders<PunishmentMessagePlaceholders.PunishmentMessagePlaceholder> {
    sealed class PunishmentMessagePlaceholder(placeholders: List<LorittaPlaceholder>) : SectionPlaceholder(placeholders)

    data object UserMentionPlaceholder : PunishmentMessagePlaceholder(PlaceholderGroups.USER_MENTION)
    data object UserNamePlaceholder : PunishmentMessagePlaceholder(PlaceholderGroups.USER_NAME)
    data object UserAvatarUrlPlaceholder : PunishmentMessagePlaceholder(PlaceholderGroups.USER_AVATAR_URL)
    data object UserIdPlaceholder : PunishmentMessagePlaceholder(PlaceholderGroups.USER_ID)
    data object UserDiscriminatorPlaceholder : PunishmentMessagePlaceholder(PlaceholderGroups.USER_DISCRIMINATOR)
    data object UserTagPlaceholder : PunishmentMessagePlaceholder(PlaceholderGroups.USER_TAG)
    data object GuildNamePlaceholder : PunishmentMessagePlaceholder(PlaceholderGroups.GUILD_NAME)
    data object GuildSizePlaceholder : PunishmentMessagePlaceholder(PlaceholderGroups.GUILD_SIZE)
    data object GuildIconUrlPlaceholder : PunishmentMessagePlaceholder(PlaceholderGroups.GUILD_ICON_URL)

    data object PunishmentReasonPlaceholder : PunishmentMessagePlaceholder(listOf(Placeholders.PUNISHMENT_REASON))
    data object PunishmentTypePlaceholder : PunishmentMessagePlaceholder(listOf(Placeholders.PUNISHMENT_TYPE, Placeholders.PUNISHMENT_TYPE_SHORT))

    data object StaffMentionPlaceholder : PunishmentMessagePlaceholder(listOf(Placeholders.STAFF_MENTION))
    data object StaffNamePlaceholder : PunishmentMessagePlaceholder(listOf(Placeholders.STAFF_NAME, Placeholders.STAFF_NAME_SHORT))
    data object StaffAvatarUrlPlaceholder : PunishmentMessagePlaceholder(listOf(Placeholders.STAFF_AVATAR_URL, Placeholders.Deprecated.STAFF_AVATAR_URL))
    data object StaffDiscriminatorPlaceholder : PunishmentMessagePlaceholder(listOf(Placeholders.STAFF_DISCRIMINATOR, Placeholders.Deprecated.STAFF_DISCRIMINATOR))
    data object StaffTagPlaceholder : PunishmentMessagePlaceholder(listOf(Placeholders.STAFF_TAG))
    data object StaffIdPlaceholder : PunishmentMessagePlaceholder(listOf(Placeholders.STAFF_ID, Placeholders.Deprecated.STAFF_ID))

    override val placeholders = listOf<PunishmentMessagePlaceholder>(
        UserMentionPlaceholder,
        UserNamePlaceholder,
        UserAvatarUrlPlaceholder,
        UserIdPlaceholder,
        UserDiscriminatorPlaceholder,
        UserTagPlaceholder,

        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder,

        PunishmentReasonPlaceholder,
        PunishmentTypePlaceholder,

        StaffMentionPlaceholder,
        StaffNamePlaceholder,
        StaffAvatarUrlPlaceholder,
        StaffDiscriminatorPlaceholder,
        StaffTagPlaceholder,
        StaffIdPlaceholder,
    )
}