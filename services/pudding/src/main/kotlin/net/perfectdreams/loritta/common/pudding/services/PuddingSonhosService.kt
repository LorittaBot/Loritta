package net.perfectdreams.loritta.cinnamon.common.pudding.services

import net.perfectdreams.loritta.cinnamon.common.services.SonhosService
import net.perfectdreams.pudding.client.PuddingClient

class PuddingSonhosService(val puddingClient: PuddingClient) : SonhosService {
    override suspend fun getSonhosRankPositionBySonhos(sonhos: Long) = puddingClient.sonhos.getSonhosRankPositionBySonhos(sonhos)
}