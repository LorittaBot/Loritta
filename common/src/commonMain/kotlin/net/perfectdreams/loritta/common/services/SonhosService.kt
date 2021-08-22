package net.perfectdreams.loritta.common.services

interface SonhosService {
    suspend fun getSonhosRankPositionBySonhos(sonhos: Long): Long
}