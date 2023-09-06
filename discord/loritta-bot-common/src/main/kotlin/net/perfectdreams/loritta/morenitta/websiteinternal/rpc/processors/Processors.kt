package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors

import net.perfectdreams.loritta.morenitta.websiteinternal.InternalWebServer
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.guild.GetGuildGamerSaferConfigProcessor
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.guild.UpdateGuildGamerSaferConfigProcessor
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.loritta.GetLorittaInfoProcessor

class Processors(val internalWebServer: InternalWebServer) {
    val getLorittaInfoProcessor = GetLorittaInfoProcessor(internalWebServer.m)
    val getGuildGamerSaferConfigProcessor = GetGuildGamerSaferConfigProcessor(internalWebServer.m)
    val updateGuildGamerSaferConfigProcessor = UpdateGuildGamerSaferConfigProcessor(internalWebServer.m)
    val executeDashGuildScopedProcessor = ExecuteDashGuildScopedProcessor(internalWebServer, internalWebServer.m)
}