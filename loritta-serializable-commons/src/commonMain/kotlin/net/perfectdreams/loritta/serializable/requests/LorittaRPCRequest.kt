package net.perfectdreams.loritta.serializable.requests

import kotlinx.serialization.Serializable

@Serializable
sealed class LorittaRPCRequest

@Serializable
object GetDailyRewardStatusRequest : LorittaRPCRequest()

@Serializable
class GetDailyRewardRequest(
    val captchaToken: String,
    val questionId: String,
    val answer: Boolean,
    val dailyMultiplierGuildIdPriority: Long?,
) : LorittaRPCRequest()