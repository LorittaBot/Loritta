package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors

import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.economy.PutPowerStreamClaimedFirstSonhosRewardProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.economy.PutPowerStreamClaimedLimitedTimeSonhosRewardProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.loritta.GetSpicyInfoProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.loritta.UpdateLorittaActivityProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.users.GetUserGuildsProcessor

class Processors(val m: LorittaDashboardBackend) {
    val getUserGuildsProcessor = GetUserGuildsProcessor(m)
    val putPowerStreamClaimedFirstSonhosRewardProcessor = PutPowerStreamClaimedFirstSonhosRewardProcessor(m)
    val putPowerStreamClaimedLimitedTimeSonhosRewardProcessor = PutPowerStreamClaimedLimitedTimeSonhosRewardProcessor(m)
    val updateLorittaActivityProcessor = UpdateLorittaActivityProcessor(m)
    val getSpicyInfoProcessor = GetSpicyInfoProcessor(m)
    val executeDashGuildScopedProcessor = ExecuteDashGuildScopedProcessor(m)
}