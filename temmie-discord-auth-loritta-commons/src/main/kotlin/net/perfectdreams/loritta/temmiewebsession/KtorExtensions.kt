package net.perfectdreams.loritta.temmiewebsession

import io.ktor.server.application.*
import io.ktor.server.sessions.*

var ApplicationCall.lorittaSession: LorittaJsonWebSession
    get() {
        return this.sessions.get() ?: LorittaJsonWebSession.empty()
    }
    set(value) {
        this.sessions.set(value)
    }