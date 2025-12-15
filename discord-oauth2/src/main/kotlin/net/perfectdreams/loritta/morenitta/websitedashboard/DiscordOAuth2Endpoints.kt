package net.perfectdreams.loritta.morenitta.websitedashboard

class DiscordOAuth2Endpoints(val discordBaseUrl: String?) {
    private fun discordBaseUrl(forceDiscord: Boolean): String {
        val baseUrl = if (!forceDiscord && discordBaseUrl != null) {
            discordBaseUrl
        } else "https://discord.com/"
        return baseUrl.removeSuffix("/")
    }

    // Don't put nirn behind the OAuth2 token, if you put it behind nirn, sometimes it may hang the request forever for... some reason?
    // https://discord.com/channels/634032658275500033/812743672759189544/1436950264342118470
    val OAuth2TokenEndpoint = "${discordBaseUrl(true)}/api/v10/oauth2/token"
    val UserIdentificationEndpoint = "${discordBaseUrl(false)}/api/v10/users/@me"
    val UserGuildsEndpoint = "${discordBaseUrl(false)}/api/v10/users/@me/guilds"
}