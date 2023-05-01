package net.perfectdreams.loritta.facingworlds.common.v1

import kotlinx.serialization.Serializable

@Serializable
sealed class FacingWorldsRPCRequest

@Serializable
class PutPowerStreamClaimedLimitedTimeSonhosRewardRequest(
    val userId: Long,
    val quantity: Long,
    val streamId: Long,
    val rewardId: Long
) : FacingWorldsRPCRequest()

@Serializable
class PutPowerStreamClaimedFirstSonhosRewardRequest(
    val userId: Long,
    val quantity: Long,
    val streamId: Long
) : FacingWorldsRPCRequest()