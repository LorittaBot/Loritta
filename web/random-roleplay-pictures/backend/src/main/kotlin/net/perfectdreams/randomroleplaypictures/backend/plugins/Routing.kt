package net.perfectdreams.randomroleplaypictures.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import net.perfectdreams.randomroleplaypictures.backend.RandomRoleplayPictures
import net.perfectdreams.sequins.ktor.BaseRoute

fun Application.configureRouting(m: RandomRoleplayPictures, routes: List<BaseRoute>) {
    routing {
        for (route in routes) {
            route.register(this)
        }
    }
}