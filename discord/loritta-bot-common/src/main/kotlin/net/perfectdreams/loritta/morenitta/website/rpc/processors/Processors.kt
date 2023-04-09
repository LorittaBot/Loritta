package net.perfectdreams.loritta.morenitta.website.rpc.processors

import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.rpc.processors.economy.GetDailyRewardProcessor
import net.perfectdreams.loritta.morenitta.website.rpc.processors.economy.GetDailyRewardStatusProcessor
import net.perfectdreams.loritta.morenitta.website.rpc.processors.guild.GetGamerSaferVerifyConfigProcessor
import net.perfectdreams.loritta.morenitta.website.rpc.processors.guild.PostGamerSaferVerifyConfigProcessor

class Processors(val m: LorittaWebsite) {
    val getDailyRewardStatusProcessor = GetDailyRewardStatusProcessor(m)
    val getDailyRewardProcessor = GetDailyRewardProcessor(m)
    val getGamerSaferVerifyConfigProcessor = GetGamerSaferVerifyConfigProcessor(m)
    val postGamerSaferVerifyConfigProcessor = PostGamerSaferVerifyConfigProcessor(m)
}