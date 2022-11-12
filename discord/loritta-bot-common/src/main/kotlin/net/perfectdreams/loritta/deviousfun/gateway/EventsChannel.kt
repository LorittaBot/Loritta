package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.gateway.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import java.util.concurrent.atomic.AtomicInteger

class EventsChannel {
    private val backedChannel = Channel<Event>(Channel.UNLIMITED)
    private val backedSize = AtomicInteger()
    val size: Int
        get() = backedSize.get()

    suspend fun send(event: Event) {
        backedSize.incrementAndGet()
        backedChannel.send(event)
    }

    operator fun iterator() = EventsChannelIterator(this, backedChannel.iterator())

    fun close() = backedChannel.close()

    class EventsChannelIterator(private val backedChannel: EventsChannel, private val backedIterator: ChannelIterator<Event>) :
        ChannelIterator<Event> {
        override suspend fun hasNext(): Boolean {
            return backedIterator.hasNext()
        }

        override fun next(): Event {
            backedChannel.backedSize.decrementAndGet()
            return backedIterator.next()
        }
    }
}