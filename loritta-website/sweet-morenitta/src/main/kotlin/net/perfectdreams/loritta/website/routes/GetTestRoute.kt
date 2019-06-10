package net.perfectdreams.loritta.website.routes

import io.ktor.application.ApplicationCall
import io.ktor.response.respondText

class GetTestRoute : BaseRoute("/owo/{uwuType?}") {
    override suspend fun onRequest(call: ApplicationCall) {
        call.respondText("Your uwu type is ${call.parameters["uwuType"]}")
    }
}