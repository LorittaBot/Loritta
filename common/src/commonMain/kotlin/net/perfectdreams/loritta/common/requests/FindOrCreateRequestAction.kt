package net.perfectdreams.loritta.common.requests

interface FindOrCreateRequestAction<T> {
    suspend fun retrieveOrCreate(): T
}