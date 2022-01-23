package net.perfectdreams.loritta.cinnamon.microservices.statscollector.endpoints

interface StatsSender {
    suspend fun send(guildCount: Long)
}