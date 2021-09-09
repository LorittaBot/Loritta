package net.perfectdreams.loritta.cinnamon.common.services

interface SonhosService {
    suspend fun getSonhosRankPositionBySonhos(sonhos: Long): Long
}