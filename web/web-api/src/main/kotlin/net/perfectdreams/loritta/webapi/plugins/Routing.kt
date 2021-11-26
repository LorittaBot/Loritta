package net.perfectdreams.loritta.webapi.plugins

import io.ktor.application.*
import io.ktor.routing.*
import net.perfectdreams.sequins.ktor.BaseRoute

fun Application.configureRouting(routes: List<BaseRoute>) {
    routing {
        trace {
            println(it.buildText())
        }

        for (route in routes)
            route.register(this)
    }
}