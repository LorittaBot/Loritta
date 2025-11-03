package net.perfectdreams.loritta.morenitta.websitedashboard

import net.perfectdreams.loritta.morenitta.LorittaBot

class DiscordOAuth2Endpoints(val loritta: LorittaBot) {
    private fun discordBaseUrl(): String {
        val baseUrl = if (loritta.config.loritta.discord.baseUrl != null) {
            loritta.config.loritta.discord.baseUrl
        } else "https://discord.com/"
        return baseUrl.removeSuffix("/")
    }

    val OAuth2TokenEndpoint = "${discordBaseUrl()}/token/oauth2"
    val UserIdentificationEndpoint = "${discordBaseUrl()}/api/v10/users/@me"
    val UserGuildsEndpoint = "${discordBaseUrl()}/api/v10/users/@me/guilds"
}