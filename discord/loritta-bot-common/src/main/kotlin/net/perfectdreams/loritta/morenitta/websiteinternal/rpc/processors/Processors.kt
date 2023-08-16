package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors

import net.perfectdreams.loritta.morenitta.websiteinternal.InternalWebServer
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.guild.GetGuildGamerSaferConfigProcessor
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.guild.GetGuildInfoProcessor
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.loritta.GetLorittaInfoProcessor
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.guild.UpdateGuildGamerSaferConfigProcessor

class Processors(val internalWebServer: InternalWebServer) {
    val getGuildInfoProcessor = GetGuildInfoProcessor(internalWebServer.m)
    val getLorittaInfoProcessor = GetLorittaInfoProcessor(internalWebServer.m)
    val getGuildGamerSaferConfigProcessor = GetGuildGamerSaferConfigProcessor(internalWebServer.m)
    val updateGuildGamerSaferConfigProcessor = UpdateGuildGamerSaferConfigProcessor(internalWebServer.m)
}