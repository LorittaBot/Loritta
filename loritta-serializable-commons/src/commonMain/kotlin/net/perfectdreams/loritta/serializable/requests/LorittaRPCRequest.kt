package net.perfectdreams.loritta.serializable.requests

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.GamerSaferVerificationUserAndRole

@Serializable
sealed class LorittaRPCRequest

@Serializable
object GetDailyRewardStatusRequest : LorittaRPCRequest()

@Serializable
class GetDailyRewardRequest(
    val captchaToken: String,
    val questionId: String,
    val answerIndex: Int,
    val dailyMultiplierGuildIdPriority: Long?,
    val fingerprint: Fingerprint
) : LorittaRPCRequest() {
    @Serializable
    data class Fingerprint(
        val width: Int,
        val height: Int,
        val availWidth: Int,
        val availHeight: Int,
        val timezoneOffset: Int,
        val clientId: String
    )
}

@Serializable
class GetGamerSaferVerifyConfigRequest(
    val guildId: Long
) : LorittaRPCRequest()

@Serializable
class PostGamerSaferVerifyConfigRequest(
    val guildId: Long,
    val enabled: Boolean,
    val verifiedRoleId: Long?,
    val verificationRoles: List<GamerSaferVerificationUserAndRole>
) : LorittaRPCRequest()