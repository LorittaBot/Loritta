package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn

public object DiscordCdn {
    private const val BASE_URL = "https://cdn.discordapp.com"

    public fun emoji(emojiId: ULong): CdnUrl = CdnUrl("$BASE_URL/emojis/$emojiId")

    public fun defaultAvatar(discriminator: Int): CdnUrl = CdnUrl("$BASE_URL/embed/avatars/${discriminator % 5}")

    public fun userAvatar(userId: ULong, hash: String): CdnUrl = CdnUrl("$BASE_URL/avatars/$userId/$hash")

    public fun memberAvatar(guildId: ULong, userId: ULong, hash: String): CdnUrl =
        CdnUrl("$BASE_URL/guilds/$guildId/users/$userId/avatars/$hash")

    public fun roleIcon(roleId: ULong, hash: String): CdnUrl = CdnUrl("$BASE_URL/role-icons/$roleId/$hash")
}