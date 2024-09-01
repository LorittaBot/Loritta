package net.perfectdreams.yokye

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration

class CoroutineQueue<T>(capacity: Int) {
    private val channel = Channel<T>(capacity)
    private val count = AtomicInteger(0)

    fun trySend(item: T) {
        val result = channel.trySend(item)
        if (result.isSuccess)
            count.incrementAndGet()
    }

    suspend fun send(item: T) {
        channel.send(item)
        count.incrementAndGet()
    }

    suspend fun receive(): T {
        val item = channel.receive()
        count.decrementAndGet()
        return item
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun receiveAll(): List<T> {
        val items = mutableListOf<T>()
        // First we'll do a non-isEmpty check, because we WANT this to suspend if there isn't anything on the channel
        items.add(receive())
        while (!channel.isEmpty) {
            items.add(receive())
        }
        return items
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun receiveAll(minimum: Int, maximumQueryTimeout: Duration): List<T> {
        val items = mutableListOf<T>()
        // First we'll do a non-isEmpty check, because we WANT this to suspend if there isn't anything on the channel
        items.add(receive())
        try {
            withTimeout(maximumQueryTimeout) {
                while (!channel.isEmpty || minimum > items.size) {
                    items.add(receive())
                }
            }
        } catch (_: TimeoutCancellationException) {
            // We don't care about timeouts
        }
        return items
    }

    fun getCount(): Int {
        return count.get()
    }
}