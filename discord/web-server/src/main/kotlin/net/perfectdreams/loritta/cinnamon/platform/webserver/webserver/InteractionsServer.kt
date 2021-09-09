package net.perfectdreams.loritta.cinnamon.platform.webserver.webserver

import dev.kord.common.entity.Snowflake
import dev.kord.rest.service.RestClient
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.webserver.DefaultInteractionRequestHandler
import net.perfectdreams.discordinteraktions.webserver.installDiscordInteractions
import net.perfectdreams.loritta.cinnamon.platform.webserver.webserver.routes.api.v1.cinnamon.GetPrometheusMetricsRoute
import net.perfectdreams.sequins.ktor.BaseRoute

/**
 * Class represents an Rest Interactions Server, which'll connect
 * to the Discord API and wrap your requests.
 *
 * @param applicationId Your bot ID/Client ID (https://i.imgur.com/075OBWk.png)
 * @param publicKey The public key of your bot (https://i.imgur.com/xDZnJ5J.png)
 * @param token Your bot token (https://i.imgur.com/VXLOFte.png)
 * @param port HTTP server port to bind
 */
class InteractionsServer(
    val rest: RestClient,
    val applicationId: Long,
    val publicKey: String,
    val port: Int = 12212
) {
    val commandManager = CommandManager()
    val interactionRequestHandler = DefaultInteractionRequestHandler(
        Snowflake(applicationId),
        commandManager,
        rest
    )
    val routes = listOf<BaseRoute>(
        GetPrometheusMetricsRoute()
    )

    /**
     * You can use this method to start the interactions server,
     * which will open an connection on the 12212 port with the **Netty** engine.
     */
    fun start() {
        val server = embeddedServer(Netty, port = port) {
            routing {
                get("/") {
                    call.respondText("Cinnamon Slash Commands Web Server")
                }

                installDiscordInteractions(
                    publicKey,
                    "/",
                    interactionRequestHandler
                )

                for (route in routes) {
                    route.register(this)
                }
            }
        }

        server.start(true)
    }
}