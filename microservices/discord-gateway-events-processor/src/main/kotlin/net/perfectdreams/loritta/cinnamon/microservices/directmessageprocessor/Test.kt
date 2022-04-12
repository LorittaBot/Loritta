package net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor

import dev.kord.gateway.Event
import kotlinx.serialization.json.Json

fun main() {
    val i = "{\"op\":0,\"t\":\"MESSAGE_UPDATE\",\"s\":47,\"d\":{\"id\":\"963152744691155034\",\"embeds\":[{\"url\":\"https://www.youtube.com/watch?v=sQEXgplxozM\",\"type\":\"video\",\"color\":16711680,\"title\":\"[+18] CASAMENTO DA KATE CIDADE ALTA - VENTURA\",\"video\":{\"url\":\"https://www.youtube.com/embed/sQEXgplxozM\",\"width\":1280,\"height\":720},\"author\":{\"url\":\"https://www.youtube.com/channel/UC682LARVpBwLTiYhi88UofA\",\"name\":\"OCRISTIANO96\"},\"provider\":{\"url\":\"https://www.youtube.com\",\"name\":\"YouTube\"},\"thumbnail\":{\"url\":\"https://i.ytimg.com/vi/sQEXgplxozM/sddefault.jpg\",\"width\":640,\"height\":480,\"proxy_url\":\"https://images-ext-2.discordapp.net/external/fsuIuzgkxXNa7n1icGZrskghaN91sPcmASiN1Qa6nw8/https/i.ytimg.com/vi/sQEXgplxozM/sddefault.jpg\"},\"description\":\"Broadcasted live on Twitch -- Watch live at https://www.twitch.tv/ventura_rp\"}],\"guild_id\":\"297732013006389252\",\"channel_id\":\"509043859792068609\"}}"
    println(
        Json.decodeFromString(
            Event.DeserializationStrategy,
            i
        )
    )
}