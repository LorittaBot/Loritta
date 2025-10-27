package net.perfectdreams.loritta.placeholders

object PlaceholderGroups {
    val USER_MENTION = listOf(Placeholders.USER_MENTION)
    val USER_NAME = listOf(Placeholders.USER_NAME, Placeholders.USER_NAME_SHORT)
    val USER_DISCRIMINATOR = listOf(Placeholders.USER_DISCRIMINATOR, Placeholders.Deprecated.USER_DISCRIMINATOR)
    val USER_TAG = listOf(Placeholders.USER_TAG)

    val GUILD_NAME = listOf(Placeholders.GUILD_NAME, Placeholders.GUILD_NAME_SHORT)
    val GUILD_SIZE = listOf(Placeholders.GUILD_SIZE, Placeholders.Deprecated.GUILD_SIZE, Placeholders.Deprecated.GUILD_SIZE_JOINED)
    val GUILD_ICON_URL = listOf(Placeholders.GUILD_ICON_URL, Placeholders.Deprecated.GUILD_ICON_URL)

}