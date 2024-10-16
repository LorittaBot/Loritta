package net.perfectdreams.loritta.morenitta.analytics.stats

interface StatsSender {
    suspend fun send(guildCount: Long, totalSonhos: Long, totalSonhosOfBannedUsers: Long)
}