package net.perfectdreams.loritta.website.ws

import io.ktor.routing.Routing
import io.ktor.websocket.webSocket
import net.perfectdreams.loritta.website.LorittaWebsite

fun Routing.webSocket(m: LorittaWebsite) {
    webSocket("/ws") { // websocketSession
        /* val ogSession = call.sessions.get<SampleSession>() ?: net.perfectdreams.loritta.website.SampleSession(
            uniqueId = java.util.UUID.randomUUID(),
            serializedDiscordAuth = null
        )
        val list2 = m.webSocketSessions.computeIfAbsent(ogSession.uniqueId) { java.util.concurrent.CopyOnWriteArrayList<WebSocketSession>() }
        list2.add(this)

        m.sendState(this, ogSession)

        try {
            while (true) {
                val frame = incoming.receive()
                when (frame) {
                    is Frame.Text -> {
                        val jsonPayload = frame.readText()

                        val json = com.google.gson.JsonParser().parse(jsonPayload).obj

                        val type = json["type"].string
                        if (type == "ping") {
                            outgoing.send(
                                io.ktor.http.cio.websocket.Frame.Text(
                                    com.google.gson.Gson().toJson(
                                        com.github.salomonbrys.kotson.jsonObject(
                                            "type" to "ping"
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
            }
        } finally {
            println("WebSocket connection closed!")
            list2.remove(this)
            if (list2.isEmpty()) {
                m.webSocketSessions.remove(ogSession.uniqueId)
                println("All WebSockets connections from the client were finished!")
            }
        } */
    }
}