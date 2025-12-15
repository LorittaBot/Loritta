package net.perfectdreams.loritta.morenitta.websitedashboard

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.content.TextContent
import io.ktor.http.formUrlEncode
import io.ktor.http.userAgent
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Authorization
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.AuthenticationStateUtils
import java.util.Base64
import javax.swing.plaf.nimbus.State

class DiscordOAuth2Manager(
    val http: HttpClient,
    discordBaseUrl: String?
) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
        const val USER_AGENT = "Loritta-Morenitta-Discord-Auth/2.0"
    }

    val oauth2Endpoints = DiscordOAuth2Endpoints(discordBaseUrl)

    suspend fun <StateType> authenticate(
        applicationId: Long,
        clientSecret: String,
        redirectUri: String,
        requiredAuthorizedScopes: List<String>,
        accessCode: String?,
        stateAsString: String?,
        stateDeserializer: DeserializationStrategy<StateType>?,
        authenticationStateKey: String?,
        error: String?,
        errorDescription: String?
    ): DiscordAuthenticationResult {
        val state = if (stateAsString != null) {
            if (stateDeserializer == null)
                error("Missing state deserializer!")
            if (authenticationStateKey == null)
                error("Missing authentication state key!")

            try {
                val decodedData = AuthenticationStateUtils.verifyAndExtract(
                    Base64.getUrlDecoder().decode(stateAsString).toString(Charsets.UTF_8),
                    authenticationStateKey
                )

                if (decodedData == null)
                    return DiscordAuthenticationResult.TamperedState

                Json.decodeFromString(stateDeserializer, decodedData)
            } catch (e: Exception) {
                logger.info(e) { "User authentication failed! State failed to be validated and decoded. State was $stateAsString" }
                return DiscordAuthenticationResult.TamperedState
            }
        } else null

        if (error != null) {
            // oof, something went wrong!
            logger.info { "User authentication failed! Error was $error, error description was $errorDescription" }
            return DiscordAuthenticationResult.ClientSideError(
                error,
                errorDescription
            )
        }

        if (accessCode == null) {
            return DiscordAuthenticationResult.MissingAccessCode
        }

        // Attempt to authorize the user!
        val parameters = Parameters.build {
            append("client_id", applicationId.toString())
            append("client_secret", clientSecret)
            append("grant_type", "authorization_code")
            append("code", accessCode)
            append("redirect_uri", redirectUri)
        }

        val oauth2TokenHttpResponse = http.post {
            url(oauth2Endpoints.OAuth2TokenEndpoint)
            userAgent(USER_AGENT)

            setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
        }

        logger.info { "Authentication Result (Status Code: ${oauth2TokenHttpResponse.status}): $oauth2TokenHttpResponse" }

        if (oauth2TokenHttpResponse.status == HttpStatusCode.InternalServerError) {
            logger.info { "User authentication failed! Discord sent a internal server error during OAuth2 token request" }
            return DiscordAuthenticationResult.DiscordInternalServerError
        }

        val oauth2TokenResultAsText = oauth2TokenHttpResponse.bodyAsText()
        val resultAsJson = Json.parseToJsonElement(oauth2TokenResultAsText).jsonObject

        if (resultAsJson.containsKey("error")) {
            val error = resultAsJson["error"]!!.jsonPrimitive.content
            val errorDescription = resultAsJson["error_description"]?.jsonPrimitive?.content
            logger.info { "User authentication failed! Error was $error, error description was $errorDescription" }
            return DiscordAuthenticationResult.TokenExchangeError(
                error,
                errorDescription
            )
        }

        val result = Json.decodeFromJsonElement<DiscordOAuth2Authorization>(resultAsJson)

        // When testing this: Remember that Discord "persists" your previously authorized scopes
        val authorizedScopes = result.scope.split(" ")
        val hasAllRequiredScopes = requiredAuthorizedScopes.all { it in authorizedScopes }
        if (!hasAllRequiredScopes) {
            logger.info { "User authentication failed! We need $requiredAuthorizedScopes but user only authorized $authorizedScopes" }
            return DiscordAuthenticationResult.MissingScopes(authorizedScopes)
        }

        // We also want to get the user's information and associate it with the session
        // This way, we can avoid doing round trips every time when sending requests "on behalf" of the user
        // Because we already have the user's IDs!
        val userIdentificationHttpResponse = http.get {
            url(oauth2Endpoints.UserIdentificationEndpoint)
            userAgent(USER_AGENT)

            header("Authorization", "Bearer ${result.accessToken}")
        }

        val userIdentificationAsText = userIdentificationHttpResponse.bodyAsText()
        logger.info { "User Identification Result (${userIdentificationHttpResponse.status}): $userIdentificationAsText" }

        if (oauth2TokenHttpResponse.status == HttpStatusCode.InternalServerError) {
            logger.info { "User authentication failed! Discord sent a internal server error during user identification request" }
            return DiscordAuthenticationResult.DiscordInternalServerError
        }

        val userIdentification = Json.decodeFromString<DiscordOAuth2UserIdentification>(userIdentificationAsText)

        return DiscordAuthenticationResult.Success(
            authorization = result,
            userIdentification = userIdentification,
            state = state
        )
    }
}