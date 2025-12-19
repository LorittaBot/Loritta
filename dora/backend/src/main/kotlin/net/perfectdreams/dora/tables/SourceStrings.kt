package net.perfectdreams.dora.tables

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.LongIdTable

object SourceStrings : LongIdTable() {
    val project = reference("project", Projects).index()
    val key = text("key").index()
    val text = text("text")
    val context = text("context").nullable()

    init {
        uniqueIndex(project, key)
    }
}