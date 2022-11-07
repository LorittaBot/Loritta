package net.perfectdreams.loritta.deviousfun.cache

sealed class DatabaseCacheValue<T> {
    class Null<T> : DatabaseCacheValue<T>()
    class Value<T>(val data: T) : DatabaseCacheValue<T>()
}