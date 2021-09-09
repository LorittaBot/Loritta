package net.perfectdreams.loritta.cinnamon.common.requests

interface RequestAction<T> {
    suspend fun retrieve(): T
}