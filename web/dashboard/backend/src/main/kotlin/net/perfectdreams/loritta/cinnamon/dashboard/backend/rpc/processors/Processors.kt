package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors

import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.guild.GetGuildGamerSaferConfigProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.guild.GetGuildInfoProcessor
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.guild.UpdateGuildGamerSaferConfigProcessor

class Processors(val m: LorittaDashboardBackend) {
    val getGuildInfoProcessor = GetGuildInfoProcessor(m)
    val getGuildGamerSaferConfigProcessor = GetGuildGamerSaferConfigProcessor(m)
    val updateGuildGamerSaferConfigProcessor = UpdateGuildGamerSaferConfigProcessor(m)
}