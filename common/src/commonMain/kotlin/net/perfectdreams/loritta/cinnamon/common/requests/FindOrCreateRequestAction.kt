package net.perfectdreams.loritta.cinnamon.common.requests

interface FindOrCreateRequestAction<T> {
    suspend fun retrieveOrCreate(): T
}