package net.perfectdreams.loritta.temmiewebsession

import io.ktor.server.application.*
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

fun LorittaTemmieDiscordAuth(
    call: ApplicationCall,
    clientId: String,
    clientSecret: String,
    authCode: String?,
    redirectUri: String,
    scope: List<String>,
    accessToken: String? = null,
    refreshToken: String? = null,
    expiresIn: Long? = null,
    generatedAt: Long? = null
) = TemmieDiscordAuth(
    clientId,
    clientSecret,
    authCode,
    redirectUri,
    scope,
    accessToken,
    refreshToken,
    expiresIn,
    generatedAt,
    onTokenChange = {
        LorittaWebSession.ON_TOKEN_CHANGE_BEHAVIOR(call, it)
    }
)