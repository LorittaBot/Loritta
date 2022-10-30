package net.perfectdreams.loritta.deviouscache.server.tables

import org.jetbrains.exposed.sql.Table

object GatewaySessions : Table() {
    val id = integer("id").index()
    val sessionId = text("session_id")
    val resumeGatewayUrl = text("resume_gateway_url")
    val sequence = integer("sequence")

    override val primaryKey = PrimaryKey(id)
}