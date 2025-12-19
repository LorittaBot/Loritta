package net.perfectdreams.dora.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Projects : LongIdTable() {
    val slug = text("slug").uniqueIndex()
    val fancyName = text("fancy_name")
    val description = text("description")
    val sourceLanguageId = text("source_language_id")
    val sourceLanguageName = text("source_language_name")
    val languagesFolder = text("language_folder")
    val sourceBranch = text("source_branch")
    val repositoryUrl = text("repository_url")
    val iconUrl = text("icon_url").nullable()
}