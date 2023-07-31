package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

sealed class Resource<out T> {
    class Success<out T>(val value: T) : Resource<T>()
    class Loading<T> : Resource<T>()
    class Failure<T>(val exception: Exception?) : Resource<T>()
}