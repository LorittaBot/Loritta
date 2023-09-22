package net.perfectdreams.loritta.common.utils.placeholders

import net.perfectdreams.loritta.i18n.I18nKeysData

data object JoinMessagePlaceholders : SectionPlaceholders<JoinMessagePlaceholders.JoinMessagePlaceholder> {
    override val type = PlaceholderSectionType.JOIN_MESSAGE

    sealed interface JoinMessagePlaceholder : MessagePlaceholder

    object UserMentionPlaceholder : GenericPlaceholders.UserMentionPlaceholder(I18nKeysData.Placeholders.JoinMessage.UserMention), JoinMessagePlaceholder
    object UserNamePlaceholder : GenericPlaceholders.UserNamePlaceholder(I18nKeysData.Placeholders.JoinMessage.UserName), JoinMessagePlaceholder
    object UserDiscriminatorPlaceholder : GenericPlaceholders.UserDiscriminatorPlaceholder(null), JoinMessagePlaceholder
    object UserTagPlaceholder : GenericPlaceholders.UserTagPlaceholder(I18nKeysData.Placeholders.JoinMessage.UserTag), JoinMessagePlaceholder
    object UserIdPlaceholder : GenericPlaceholders.UserIdPlaceholder(I18nKeysData.Placeholders.JoinMessage.UserId), JoinMessagePlaceholder
    object UserAvatarUrlPlaceholder : GenericPlaceholders.UserAvatarUrlPlaceholder(I18nKeysData.Placeholders.JoinMessage.UserAvatarUrl), JoinMessagePlaceholder
    object GuildNamePlaceholder : GenericPlaceholders.GuildNamePlaceholder(I18nKeysData.Placeholders.Generic.GuildName), JoinMessagePlaceholder
    object GuildSizePlaceholder : GenericPlaceholders.GuildSizePlaceholder(I18nKeysData.Placeholders.Generic.GuildSize), JoinMessagePlaceholder

    override val placeholders = listOf<JoinMessagePlaceholder>(
        UserMentionPlaceholder,
        UserNamePlaceholder,
        UserDiscriminatorPlaceholder,
        UserTagPlaceholder,
        UserIdPlaceholder,
        UserAvatarUrlPlaceholder,
        GuildNamePlaceholder,
        GuildSizePlaceholder
    )
}