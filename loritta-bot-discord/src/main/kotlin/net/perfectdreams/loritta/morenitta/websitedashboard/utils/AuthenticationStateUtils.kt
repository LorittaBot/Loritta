package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.concrete.GroupChannel
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.websitedashboard.AuthenticationState
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object AuthenticationStateUtils {
    private const val ALGORITHM = "HmacSHA256"

    fun createDiscordSourceTrackingString(channel: Channel): String {
        return buildString {
            if (channel is GuildChannel) {
                append("guild_${channel.guild.id}_channel_${channel.id}")
            } else if (channel is GroupChannel) {
                append("groupdm_${channel.id}")
            } else if (channel is PrivateChannel) {
                append("dm_${channel.id}")
            } else {
                // We don't know what this is! :(
                append("channel_${channel.id}")
            }
        }
    }

    fun createStateAsBase64(state: AuthenticationState, loritta: LorittaBot): String {
        return Base64.getUrlEncoder()
            .encodeToString(
                signAndCombine(
                    Json.encodeToString(state),
                    loritta.config.loritta.dashboard.authenticationStateKey
                ).toByteArray(Charsets.UTF_8)
            )
    }

    fun createStateAsBase64(state: AuthenticationState, secretKey: String): String {
        return Base64.getUrlEncoder()
            .encodeToString(
                signAndCombine(
                    Json.encodeToString(state),
                    secretKey
                ).toByteArray(Charsets.UTF_8)
            )
    }

    fun sign(data: String, secretKey: String): String {
        val mac = Mac.getInstance(ALGORITHM)
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), ALGORITHM)
        mac.init(secretKeySpec)

        val signatureBytes = mac.doFinal(data.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes)
    }

    fun verify(data: String, signature: String, secretKey: String): Boolean {
        val expectedSignature = sign(data, secretKey)
        return signature == expectedSignature
    }

    fun signAndCombine(jsonString: String, secretKey: String): String {
        val signature = sign(jsonString, secretKey)
        return "${Base64.getEncoder().encodeToString(jsonString.toByteArray(Charsets.UTF_8))}.$signature"
    }

    fun verifyAndExtract(signedData: String, secretKey: String): String? {
        val parts = signedData.split(".", limit = 2)
        if (parts.size != 2) return null

        val (dataEncoded, signatureEncoded) = parts

        val data = Base64.getUrlDecoder().decode(dataEncoded).toString(Charsets.UTF_8)
        return if (verify(data, signatureEncoded, secretKey)) data else null
    }
}