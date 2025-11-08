package net.perfectdreams.loritta.common.utils

import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

val LORITTA_AUTHORIZATION_SCOPES = listOf("identify", "guilds", "email")
val LORITTA_ADD_BOT_SCOPES = LORITTA_AUTHORIZATION_SCOPES + listOf("bot", "applications.commands")

/**
 * Builds a Discord OAuth2 authorization URL
 *
 * @params parameters the OAuth2 URL parameters (such as `client_id`, etc)
 */
fun DiscordOAuth2AuthorizationURL(
    parameters: ParametersBuilder.() -> (Unit)
) = URLBuilder(
    protocol = URLProtocol.HTTPS,
    host = "discord.com",
    pathSegments = listOf("oauth2", "authorize"),
    parameters = ParametersBuilder().apply(parameters).build()
).build()

/**
 * Builds Loritta's Discord OAuth2 Add Bot `authorize` URL
 *
 * @params clientId the bot's ID
 * @params redirectUri where the user will be redirected after authenticating on Discord
 * @params guildId the guild ID that will be prefilled in the add bot modal
 * @params redirectAfterAuthenticationUrl where the user will be redirected after their authentication has been validated
 * @params parameters additional OAuth2 URL parameters
 */
@OptIn(ExperimentalEncodingApi::class)
fun LorittaDiscordOAuth2AddBotURL(
    clientId: Long,
    redirectUri: String,
    guildId: Long? = null,
    state: String? = null,
    parameters: ParametersBuilder.() -> (Unit) = {}
) = DiscordOAuth2AuthorizationURL {
    append("client_id", clientId.toString())
    append("scope", LORITTA_ADD_BOT_SCOPES.joinToString(" "))
    append("permissions", "2080374975")
    append("response_type", "code")
    append("redirect_uri", redirectUri)
    if (guildId != null)
        append("guild_id", guildId.toString())
    if (state != null)
        append("state", state)

    apply(parameters)
}

/**
 * Builds Loritta's Discord OAuth2 `authorize` URL
 *
 * @params clientId the bot's ID
 * @params redirectUri where the user will be redirected after authenticating on Discord
 * @params redirectAfterAuthenticationUrl where the user will be redirected after their authentication has been validated
 * @params parameters additional OAuth2 URL parameters
 */
@OptIn(ExperimentalEncodingApi::class)
fun LorittaDiscordOAuth2AuthorizeScopeURL(
    clientId: Long,
    redirectUri: String,
    state: String? = null,
    parameters: ParametersBuilder.() -> (Unit) = {}
) = DiscordOAuth2AuthorizationURL {
    append("client_id", clientId.toString())
    append("scope", LORITTA_AUTHORIZATION_SCOPES.joinToString(" "))
    append("response_type", "code")
    append("redirect_uri", redirectUri)
    if (state != null)
        append("state", Base64.UrlSafe.encode(state.toString().toByteArray(Charsets.UTF_8)))

    apply(parameters)
}