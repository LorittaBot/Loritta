package net.perfectdreams.loritta.common.requests

interface RequestAction<T> {
    suspend fun retrieve(): T
}