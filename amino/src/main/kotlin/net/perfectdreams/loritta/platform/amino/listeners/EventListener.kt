package net.perfectdreams.loritta.platform.amino.listeners

import net.perfectdreams.aminoreapi.events.message.MessageReceivedEvent
import net.perfectdreams.aminoreapi.hooks.ListenerAdapter
import net.perfectdreams.loritta.platform.amino.AminoLoritta

class EventListener(val loritta: AminoLoritta) : ListenerAdapter() {
    override suspend fun onMessageReceived(event: MessageReceivedEvent) {
        println("Received a new message! ${event.message.author.nickname}: ${event.message.content}")

        if (event.message.author.nickname != "Loritta") {
            val thread = event.retrieveThread() ?: return

            println(thread.title)

            loritta.commandManager.dispatch(
                    event,
                    loritta.getLocaleById("default"),
                    loritta.getLegacyLocaleById("default")
            )
        }
    }
}