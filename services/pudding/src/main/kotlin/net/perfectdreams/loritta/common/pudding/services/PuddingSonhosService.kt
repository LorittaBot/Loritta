package net.perfectdreams.loritta.common.pudding.services

import net.perfectdreams.loritta.common.services.SonhosService
import net.perfectdreams.pudding.client.PuddingClient

class PuddingSonhosService(val puddingClient: PuddingClient) : SonhosService {
    override suspend fun getSonhosRankPositionBySonhos(sonhos: Long) = puddingClient.sonhos.getSonhosRankPositionBySonhos(sonhos)
}