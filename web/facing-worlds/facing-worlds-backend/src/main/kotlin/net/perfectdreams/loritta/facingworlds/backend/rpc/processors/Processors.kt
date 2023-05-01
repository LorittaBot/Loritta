package net.perfectdreams.loritta.facingworlds.backend.rpc.processors

import net.perfectdreams.loritta.facingworlds.backend.FacingWorldsBackend
import net.perfectdreams.loritta.facingworlds.backend.rpc.processors.economy.PutPowerStreamClaimedFirstSonhosRewardProcessor
import net.perfectdreams.loritta.facingworlds.backend.rpc.processors.economy.PutPowerStreamClaimedLimitedTimeSonhosRewardProcessor

class Processors(val m: FacingWorldsBackend) {
    val putPowerStreamClaimedLimitedTimeSonhosRewardProcessor = PutPowerStreamClaimedLimitedTimeSonhosRewardProcessor(m)
    val putPowerStreamClaimedFirstSonhosRewardProcessor = PutPowerStreamClaimedFirstSonhosRewardProcessor(m)
}