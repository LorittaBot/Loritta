package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi

import io.ktor.server.application.*

class WebsitePublicAPIException(val action: suspend (ApplicationCall) -> (Unit)) : RuntimeException()