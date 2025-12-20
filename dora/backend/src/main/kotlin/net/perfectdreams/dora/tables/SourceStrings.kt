package net.perfectdreams.dora.tables

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object SourceStrings : LongIdTable() {
    val project = reference("project", Projects).index()
    val key = text("key").index()
    val text = text("text")
    val context = text("context").nullable()
    val addedAt = timestampWithTimeZone("added_at")

    init {
        uniqueIndex(project, key)
    }
}