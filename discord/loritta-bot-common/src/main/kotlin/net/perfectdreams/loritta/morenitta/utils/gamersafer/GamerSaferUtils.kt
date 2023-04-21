package net.perfectdreams.loritta.morenitta.utils.gamersafer

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.morenitta.LorittaBot
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object GamerSaferUtils {
    fun createGuildInfo(loritta: LorittaBot, guildId: Long): GamerSaferGuildInfo {
        val provider = loritta.config.loritta.gamerSafer.provider

        return GamerSaferGuildInfo(
            provider,
            guildId.toString(),
            createJWTTokenForGuild(loritta, guildId)
        )
    }

    fun createJWTTokenForGuild(loritta: LorittaBot, guildId: Long): String {
        val provider = loritta.config.loritta.gamerSafer.provider

        val header = buildJsonObject { put("alg", "HS256") }.toString()
        val data = "$provider|$guildId"

        return createJWTToken(
            header,
            data,
            loritta.config.loritta.gamerSafer.secretKey
        )
    }

    fun createJWTToken(header: String, data: String, secretKey: String): String {
        val base64WithoutPadding = Base64.getUrlEncoder().withoutPadding()

        val secretKeyAsBase64 = secretKey

        val decodedKey: ByteArray = secretKeyAsBase64.toByteArray(Charsets.UTF_8)
        val secretKey: SecretKey = SecretKeySpec(
            decodedKey,
            0,
            decodedKey.size,
            "HS256"
        )

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)

        val headerAsBase64 = base64WithoutPadding.encodeToString(header.toByteArray(Charsets.UTF_8))
        val dataAsBase64 = base64WithoutPadding.encodeToString(data.toByteArray(Charsets.UTF_8))
        val doneFinal = mac.doFinal("${headerAsBase64}.${dataAsBase64}".toByteArray(Charsets.UTF_8))
        val doneFinalAsBase64 = base64WithoutPadding.encodeToString(doneFinal)

        return "$headerAsBase64.$dataAsBase64.$doneFinalAsBase64"
    }

    data class GamerSaferGuildInfo(
        val provider: String,
        val providerId: String,
        val jws: String
    )
}