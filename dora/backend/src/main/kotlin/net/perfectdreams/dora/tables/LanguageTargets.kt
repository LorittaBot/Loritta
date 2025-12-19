package net.perfectdreams.dora.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object LanguageTargets : LongIdTable() {
    val project = reference("project", Projects)
    val languageId = text("language_id")
    val languageName = text("language_name")
    // val folderTarget = text("folder_target")

    init {
        uniqueIndex(project, languageId)
    }
}