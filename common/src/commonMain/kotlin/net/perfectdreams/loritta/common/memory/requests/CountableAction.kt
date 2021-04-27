package net.perfectdreams.loritta.common.pudding.requests

interface CountableAction {
    suspend fun retrieveCount(): Long
}