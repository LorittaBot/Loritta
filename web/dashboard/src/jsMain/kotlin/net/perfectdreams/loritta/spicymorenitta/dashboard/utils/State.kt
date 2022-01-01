package net.perfectdreams.loritta.spicymorenitta.dashboard.utils

sealed class State<out T> {
    class Success<out T>(val value: T) : State<T>()
    class Loading<T> : State<T>()
    class Failure<T> : State<T>()
}