package net.perfectdreams.loritta.common.requests

interface CountableAction {
    suspend fun retrieveCount(): Long
}