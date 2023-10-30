package net.perfectdreams.loritta.temmiewebsession

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.serialization.json.*
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.util.*

object LorittaWebSession {
    /**
     * The default on token change behavior used in [TemmieDiscordAuth]
     */
    val ON_TOKEN_CHANGE_BEHAVIOR: (ApplicationCall, TemmieDiscordAuth) -> (Unit) = { call, auth ->
        val session = call.sessions.get<LorittaJsonWebSession>() ?: LorittaJsonWebSession.empty()

        call.sessions.set(
            session.copy(
                base64StoredDiscordAuthTokens = Base64.getEncoder().encode(toJson(auth).toString().toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8)
            )
        )
    }

    private fun toJson(temmieDiscordAuth: TemmieDiscordAuth): JsonObject = buildJsonObject {
        put("authCode", temmieDiscordAuth.authCode)
        put("redirectUri", temmieDiscordAuth.redirectUri)
        putJsonArray("scope") {
            for (str in temmieDiscordAuth.scope) {
                add(str)
            }
        }
        put("accessToken", temmieDiscordAuth.accessToken)
        put("refreshToken", temmieDiscordAuth.refreshToken)
        put("expiresIn", temmieDiscordAuth.expiresIn)
        put("generatedAt", temmieDiscordAuth.generatedAt)
    }

    fun convertToWebSessionIdentification(userIdentification: TemmieDiscordAuth.UserIdentification): LorittaJsonWebSession.UserIdentification {
        val now = System.currentTimeMillis()

        return LorittaJsonWebSession.UserIdentification(
            userIdentification.id,
            userIdentification.username,
            userIdentification.discriminator,
            userIdentification.verified,
            userIdentification.globalName,
            userIdentification.email,
            userIdentification.avatar,
            now,
            now
        )
    }
}