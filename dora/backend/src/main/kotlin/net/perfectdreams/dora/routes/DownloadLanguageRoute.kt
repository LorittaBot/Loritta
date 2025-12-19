package net.perfectdreams.dora.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.json.Json
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.SourceStrings
import net.perfectdreams.dora.tables.TranslationsStrings
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll

class DownloadLanguageRoute(val m: DoraBackend) : RequiresProjectAuthDashboardRoute(m, "/languages/{languageId}/download") {
    val json = Json {
        this.prettyPrint = true
    }

    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        val languageId = call.parameters.getOrFail("languageId")

        val translatableStrings = m.pudding.transaction {
            val language = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.languageId eq languageId and (LanguageTargets.project eq project.id)
                }
                .first()

            SourceStrings
                .leftJoin(TranslationsStrings, { SourceStrings.id }, { TranslationsStrings.sourceString }) {
                    TranslationsStrings.language eq language[LanguageTargets.id]
                }
                .selectAll()
                .where { SourceStrings.project eq project.id }
                .orderBy(SourceStrings.id)
                .toList()
        }

        val translatorIds = translatableStrings.mapNotNull {
            it.getOrNull(TranslationsStrings.translatedBy)?.value
        }.distinct()

        val generatedBundle = m.generateLanguageBundle(translatorIds, translatableStrings)

        call.respondText(
            json.encodeToString(generatedBundle),
            ContentType.Application.Json,
        )
    }
}