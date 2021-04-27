package net.perfectdreams.loritta.common.pudding.requests

interface FindOrCreateRequestAction<T> {
    suspend fun retrieveOrCreate(): T
}