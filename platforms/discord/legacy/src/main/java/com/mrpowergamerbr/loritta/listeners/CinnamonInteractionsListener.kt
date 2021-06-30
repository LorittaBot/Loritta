package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Used for Cinnamon, we relay the raw gateway events to the process
 */
class CinnamonInteractionsListener(private val loritta: Loritta) : ListenerAdapter() {
    companion object {
        private const val INTERACTION_CREATE_EVENT_TYPE = "INTERACTION_CREATE"
    }

    override fun onRawGateway(event: RawGatewayEvent) {
        // We only care about interaction create (so, buttons or slash commands) events
        if (event.type != INTERACTION_CREATE_EVENT_TYPE)
            return

        // Send the data to Cinnamon's webserver
        GlobalScope.launch {
            loritta.http.post("http://127.0.0.1:8080") {
                body = TextContent(event.payload.toString(), ContentType.Application.Json)
            }
        }
    }
}