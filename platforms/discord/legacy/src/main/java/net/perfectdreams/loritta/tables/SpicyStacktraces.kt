package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object SpicyStacktraces : LongIdTable() {
    val message = text("message").index()
    val spicyHash = text("spicy_hash").nullable().index()
    val file = text("file")
    val line = integer("line")
    val column = integer("column")
    val userAgent = text("user_agent").nullable()
    val url = text("url")
    val spicyPath = text("spicy_path").nullable()
    val localeId = text("locale_id")
    val isLocaleInitialized = bool("is_locale_initialized")
    val userId = long("user").nullable().index()
    val currentRoute = text("current_route").nullable()
    val stack = text("stack").nullable()
    val receivedAt = long("received_at").index()
}