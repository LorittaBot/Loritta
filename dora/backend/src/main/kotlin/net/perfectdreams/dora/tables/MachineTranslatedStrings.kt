package net.perfectdreams.dora.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object MachineTranslatedStrings : LongIdTable() {
    // We don't need the project reference here, because both the language targets and the source strings know which project they are in :)
    val language = reference("language_targets", LanguageTargets)
    val sourceString = reference("source_string", SourceStrings)
    val text = text("text")
    val translatedAt = timestampWithTimeZone("translated_at").nullable()
}