package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import kotlinx.serialization.json.Json
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object AuthenticationStateUtils {
    private const val ALGORITHM = "HmacSHA256"

    // For the "whole state" we use the Base64 URL Encoder/Decoder version
    // For the inner state (the encrypted data and the data itself) we use the normal encoder
    // WE CANNOT USE THE URL DECODER TO DECODE A NORMAL BASE64, IT WILL THROW ERRORS!!
    inline fun <reified StateType> createStateAsBase64(state: StateType, secretKey: String): String {
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

        val data = Base64.getDecoder().decode(dataEncoded).toString(Charsets.UTF_8)
        return if (verify(data, signatureEncoded, secretKey)) data else null
    }
}