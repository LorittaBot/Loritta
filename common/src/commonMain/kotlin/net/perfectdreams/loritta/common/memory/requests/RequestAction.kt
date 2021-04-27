package net.perfectdreams.loritta.common.pudding.requests

interface RequestAction<T> {
    suspend fun retrieve(): T
}