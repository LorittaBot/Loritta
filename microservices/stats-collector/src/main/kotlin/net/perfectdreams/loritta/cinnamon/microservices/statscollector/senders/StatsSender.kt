package net.perfectdreams.loritta.cinnamon.microservices.statscollector.senders

interface StatsSender {
    suspend fun send(guildCount: Long)
}