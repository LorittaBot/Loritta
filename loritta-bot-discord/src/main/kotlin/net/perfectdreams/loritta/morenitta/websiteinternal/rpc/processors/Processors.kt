package net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors

import net.perfectdreams.loritta.morenitta.websiteinternal.InternalWebServer
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.loritta.GetLorittaInfoProcessor

class Processors(val internalWebServer: InternalWebServer) {
    val getLorittaInfoProcessor = GetLorittaInfoProcessor(internalWebServer.m)
}