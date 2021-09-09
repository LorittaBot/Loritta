package net.perfectdreams.loritta.cinnamon.common.requests

interface CountableAction {
    suspend fun retrieveCount(): Long
}