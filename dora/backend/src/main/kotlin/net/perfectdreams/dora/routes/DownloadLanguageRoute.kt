package net.perfectdreams.dora.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.json.*
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.Projects
import net.perfectdreams.dora.tables.SourceStrings
import net.perfectdreams.dora.tables.TranslationsStrings
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll

class DownloadLanguageRoute(val m: DoraBackend) : BaseRoute("/projects/{projectId}/languages/{languageId}/download") {
    val json = Json {
        this.prettyPrint = true
    }

    override suspend fun onRequest(call: ApplicationCall) {
        val projectId = call.parameters.getOrFail("projectId")
        val languageId = call.parameters.getOrFail("languageId")

        val translatableStrings = m.pudding.transaction {
            val project = Projects.selectAll()
                .where {
                    Projects.slug eq projectId
                }
                .first()

            val language = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.languageId eq languageId and (LanguageTargets.project eq project[Projects.id])
                }
                .first()

            SourceStrings
                .leftJoin(TranslationsStrings, { SourceStrings.id }, { TranslationsStrings.sourceString }) {
                    TranslationsStrings.language eq language[LanguageTargets.id]
                }
                .selectAll()
                .where { SourceStrings.project eq project[Projects.id] }
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