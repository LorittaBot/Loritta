package net.perfectdreams.loritta.facingworlds.common.v1

import kotlinx.serialization.Serializable

@Serializable
sealed class FacingWorldsRPCResponse

@Serializable
sealed class PutPowerStreamClaimedLimitedTimeSonhosRewardResponse : FacingWorldsRPCResponse() {
    @Serializable
    class Success : PutPowerStreamClaimedLimitedTimeSonhosRewardResponse()

    @Serializable
    class UnknownUser : PutPowerStreamClaimedLimitedTimeSonhosRewardResponse()
}

@Serializable
sealed class PutPowerStreamClaimedFirstSonhosRewardResponse : FacingWorldsRPCResponse() {
    @Serializable
    class Success : PutPowerStreamClaimedFirstSonhosRewardResponse()

    @Serializable
    class UnknownUser : PutPowerStreamClaimedFirstSonhosRewardResponse()
}