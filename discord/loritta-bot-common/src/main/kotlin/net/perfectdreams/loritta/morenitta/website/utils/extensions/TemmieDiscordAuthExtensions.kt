package net.perfectdreams.loritta.morenitta.website.utils.extensions

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import net.perfectdreams.loritta.morenitta.utils.gson
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.util.*

fun TemmieDiscordAuth.toJsonBase64(): String {
    return Base64.getEncoder().encodeToString(
        gson.toJson(
            jsonObject(
                "authCode" to this.authCode,
                "redirectUri" to this.redirectUri,
                "scope" to this.scope.toJsonArray(),
                "accessToken" to this.accessToken,
                "refreshToken" to this.refreshToken,
                "expiresIn" to this.expiresIn,
                "generatedAt" to this.generatedAt
            )
        ).toByteArray(Charsets.UTF_8)
    )
}

fun TemmieDiscordAuth.UserIdentification.toWebSessionIdentification(): LorittaJsonWebSession.UserIdentification {
    val now = System.currentTimeMillis()

    return LorittaJsonWebSession.UserIdentification(
        id,
        username,
        discriminator,
        verified,
        email,
        avatar,
        now,
        now
    )
}