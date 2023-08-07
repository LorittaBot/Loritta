package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors

import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.economy.PutPowerStreamClaimedFirstSonhosRewardProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.economy.PutPowerStreamClaimedLimitedTimeSonhosRewardProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.guild.GetGuildGamerSaferConfigProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.guild.GetGuildInfoProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.guild.UpdateGuildGamerSaferConfigProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.users.GetUserGuildsProcessor

class Processors(val m: LorittaDashboardBackend) {
    val getGuildInfoProcessor = GetGuildInfoProcessor(m)
    val getUserGuildsProcessor = GetUserGuildsProcessor(m)
    val getGuildGamerSaferConfigProcessor = GetGuildGamerSaferConfigProcessor(m)
    val updateGuildGamerSaferConfigProcessor = UpdateGuildGamerSaferConfigProcessor(m)
    val putPowerStreamClaimedFirstSonhosRewardProcessor = PutPowerStreamClaimedFirstSonhosRewardProcessor(m)
    val putPowerStreamClaimedLimitedTimeSonhosRewardProcessor = PutPowerStreamClaimedLimitedTimeSonhosRewardProcessor(m)
}