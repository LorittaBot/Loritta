package net.perfectdreams.loritta.common.utils.placeholders

import net.perfectdreams.loritta.i18n.I18nKeysData

@Deprecated("This should not be used, use the new placeholder system instead.")
data object LeaveMessagePlaceholders : SectionPlaceholders<LeaveMessagePlaceholders.LeaveMessagePlaceholder> {
    override val type = PlaceholderSectionType.LEAVE_MESSAGE

    sealed interface LeaveMessagePlaceholder : MessagePlaceholder

    object UserMentionPlaceholder : GenericPlaceholders.UserMentionPlaceholder(I18nKeysData.Placeholders.LeaveMessage.UserMention), LeaveMessagePlaceholder
    object UserNamePlaceholder : GenericPlaceholders.UserNamePlaceholder(I18nKeysData.Placeholders.LeaveMessage.UserName), LeaveMessagePlaceholder
    object UserDiscriminatorPlaceholder : GenericPlaceholders.UserDiscriminatorPlaceholder(null), LeaveMessagePlaceholder
    object UserTagPlaceholder : GenericPlaceholders.UserTagPlaceholder(I18nKeysData.Placeholders.LeaveMessage.UserTag), LeaveMessagePlaceholder
    object UserIdPlaceholder : GenericPlaceholders.UserIdPlaceholder(I18nKeysData.Placeholders.LeaveMessage.UserId), LeaveMessagePlaceholder
    object UserAvatarUrlPlaceholder : GenericPlaceholders.UserAvatarUrlPlaceholder(I18nKeysData.Placeholders.LeaveMessage.UserAvatarUrl), LeaveMessagePlaceholder
    object GuildNamePlaceholder : GenericPlaceholders.GuildNamePlaceholder(I18nKeysData.Placeholders.Generic.GuildName), LeaveMessagePlaceholder
    object GuildSizePlaceholder : GenericPlaceholders.GuildSizePlaceholder(I18nKeysData.Placeholders.Generic.GuildSize), LeaveMessagePlaceholder
    object GuildIconUrlPlaceholder : GenericPlaceholders.GuildIconUrlPlaceholder(I18nKeysData.Placeholders.Generic.GuildIconUrl), LeaveMessagePlaceholder

    override val placeholders = listOf<LeaveMessagePlaceholder>(
        UserMentionPlaceholder,
        UserNamePlaceholder,
        UserDiscriminatorPlaceholder,
        UserTagPlaceholder,
        UserIdPlaceholder,
        UserAvatarUrlPlaceholder,
        GuildNamePlaceholder,
        GuildSizePlaceholder,
        GuildIconUrlPlaceholder
    )
}