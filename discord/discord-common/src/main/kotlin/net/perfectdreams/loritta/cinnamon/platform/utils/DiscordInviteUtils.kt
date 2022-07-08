package net.perfectdreams.loritta.cinnamon.platform.utils

object DiscordInviteUtils {
    private val shortInviteRegex = Regex("(?:https?://)?discord.gg/([A-z0-9]+)")
    private val longInviteRegex = Regex("(?:https?://)?discord(?:app)?.com/invite/([A-z0-9]+)")
    val inviteCodeRegex = Regex("[A-z0-9]+")

    /**
     * Gets a Discord invite code from a URL, matched based on [shortInviteRegex] and [longInviteRegex].
     *
     * @return the code, if it was found
     */
    fun getInviteCodeFromUrl(url: String): String? {
        val shortInviteMatch = shortInviteRegex.find(url)
        if (shortInviteMatch != null)
            return shortInviteMatch.groupValues[1]

        val longInviteMatch = longInviteRegex.find(url)
        if (longInviteMatch != null)
            return longInviteMatch.groupValues[1]

        return null
    }
}