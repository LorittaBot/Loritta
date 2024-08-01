package net.perfectdreams.loritta.discordchatmessagerendererserver

import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicInteger

class CoroutineQueue<T>(capacity: Int) {
    private val channel = Channel<T>(capacity)
    private val count = AtomicInteger(0)

    suspend fun send(item: T) {
        channel.send(item)
        count.incrementAndGet()
    }

    suspend fun receive(): T {
        val item = channel.receive()
        count.decrementAndGet()
        return item
    }

    fun getCount(): Int {
        return count.get()
    }
}