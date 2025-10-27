package net.perfectdreams.loritta.common.utils.placeholders

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

@Deprecated("This should not be used, use the new placeholder system instead.")
object GenericPlaceholders {
    abstract class GenericPlaceholder(override val description: StringI18nData? = null) : MessagePlaceholder

    abstract class UserMentionPlaceholder(description: StringI18nData?) : GenericPlaceholder(description) {
        override val names = listOf(Placeholders.USER_MENTION.toVisiblePlaceholder())
        override val renderType = MessagePlaceholder.RenderType.MENTION
    }

    abstract class UserNamePlaceholder(description: StringI18nData?) : GenericPlaceholder(description) {
        override val names = listOf(Placeholders.USER_NAME_SHORT.toVisiblePlaceholder(), Placeholders.USER_NAME.toVisiblePlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }

    // This is deprecated
    abstract class UserDiscriminatorPlaceholder(description: StringI18nData?) : GenericPlaceholder(description) {
        override val names = listOf(Placeholders.USER_DISCRIMINATOR.toHiddenPlaceholder(), Placeholders.Deprecated.USER_DISCRIMINATOR.toHiddenPlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }

    abstract class UserTagPlaceholder(description: StringI18nData?) : GenericPlaceholder(description) {
        override val names = listOf(Placeholders.USER_TAG.toVisiblePlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }

    abstract class UserIdPlaceholder(description: StringI18nData?) : GenericPlaceholder(description) {
        override val names = listOf(Placeholders.USER_ID.toVisiblePlaceholder(), Placeholders.Deprecated.USER_ID.toHiddenPlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }

    abstract class UserAvatarUrlPlaceholder(description: StringI18nData?) : GenericPlaceholder(description) {
        override val names = listOf(
            Placeholders.USER_AVATAR_URL.toVisiblePlaceholder(),
            Placeholders.Deprecated.USER_AVATAR_URL.toHiddenPlaceholder()
        )
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }

    abstract class UserNicknamePlaceholder(description: StringI18nData?) : GenericPlaceholder(description) {
        override val names = listOf(Placeholders.USER_NICKNAME.toVisiblePlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }

    abstract class GuildNamePlaceholder(description: StringI18nData?) : GenericPlaceholder(description) {
        override val names = listOf(Placeholders.GUILD_NAME_SHORT.toVisiblePlaceholder(), Placeholders.GUILD_NAME.toVisiblePlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }

    abstract class GuildSizePlaceholder(description: StringI18nData?) : GenericPlaceholder(description) {
        override val names = listOf(Placeholders.GUILD_SIZE.toVisiblePlaceholder(), Placeholders.Deprecated.GUILD_SIZE.toHiddenPlaceholder(), Placeholders.Deprecated.GUILD_SIZE_JOINED.toHiddenPlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }

    abstract class GuildIconUrlPlaceholder(description: StringI18nData?) : GenericPlaceholder(description) {
        override val names = listOf(Placeholders.GUILD_ICON_URL.toVisiblePlaceholder(), Placeholders.Deprecated.GUILD_ICON_URL.toHiddenPlaceholder())
        override val renderType = MessagePlaceholder.RenderType.TEXT
    }

    /**
     * Used to create placeholders dynamically
     */
    class Placeholder(
        override val names: List<HidableLorittaPlaceholder>,
    ) : MessagePlaceholder {
        override val renderType = MessagePlaceholder.RenderType.TEXT
        override val description = null
    }
}