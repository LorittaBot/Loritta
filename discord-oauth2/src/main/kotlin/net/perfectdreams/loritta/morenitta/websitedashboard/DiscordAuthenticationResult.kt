package net.perfectdreams.loritta.morenitta.websitedashboard

import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Authorization
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification

sealed class DiscordAuthenticationResult {
    data class Success<StateType>(
        val authorization: DiscordOAuth2Authorization,
        val userIdentification: DiscordOAuth2UserIdentification,
        val state: StateType?
    ) : DiscordAuthenticationResult()
    data object MissingAccessCode : DiscordAuthenticationResult()
    data object DiscordInternalServerError : DiscordAuthenticationResult()
    data class MissingScopes(val authorizedScopes: List<String>) : DiscordAuthenticationResult()
    data class ClientSideError(val error: String, val errorDescription: String?) : DiscordAuthenticationResult()
    data class TokenExchangeError(val error: String, val errorDescription: String?) : DiscordAuthenticationResult()
    data object TamperedState : DiscordAuthenticationResult()
}