package net.perfectdreams.dora.routes.projects.languages

import io.ktor.server.application.ApplicationCall
import io.ktor.server.util.getOrFail
import kotlinx.coroutines.flow.MutableStateFlow
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.components.batchEntry
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.CachedDiscordUserIdentifications
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.SourceStrings
import net.perfectdreams.dora.tables.TranslationsStrings
import net.perfectdreams.dora.tables.Users
import net.perfectdreams.dora.utils.TranslationProgress
import net.perfectdreams.dora.utils.respondHtmlFragment
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.blissShowToast
import net.perfectdreams.luna.toasts.createEmbeddedToast
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll

class DeleteTableEntryRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/languages/{languageSlug}/table/{stringId}/{uniqueId}") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        val uniqueId = call.parameters.getOrFail("uniqueId")
        val languageSlug = call.parameters.getOrFail("languageSlug")
        val stringId = call.parameters.getOrFail("stringId")

        data class Result(val sourceStringRow: org.jetbrains.exposed.sql.ResultRow, val counts: Pair<Int, Int>)

        val result = dora.pudding.transaction {
            val language = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.languageId eq languageSlug and (LanguageTargets.project eq project.id)
                }
                .first()

            val sourceStringRow = SourceStrings
                .leftJoin(TranslationsStrings, { SourceStrings.id }, { TranslationsStrings.sourceString }) {
                    TranslationsStrings.language eq language[LanguageTargets.id]
                }
                .leftJoin(Users, { TranslationsStrings.translatedBy }, { Users.id })
                .leftJoin(CachedDiscordUserIdentifications, { Users.id }, { CachedDiscordUserIdentifications.id })
                .selectAll()
                .where {
                    SourceStrings.key eq stringId and (SourceStrings.project eq project.id)
                }
                .first()

            // Delete translation (if any) for this language + source string
            TranslationsStrings.deleteWhere {
                TranslationsStrings.language eq language[LanguageTargets.id] and (TranslationsStrings.sourceString eq sourceStringRow[SourceStrings.id])
            }

            // Recalculate progress counts
            val totalCount = SourceStrings.selectAll()
                .where { SourceStrings.project eq project.id }
                .count()
                .toInt()

            val translatedCount = TranslationsStrings.selectAll()
                .where { TranslationsStrings.language eq language[LanguageTargets.id] }
                .count()
                .toInt()

            Result(sourceStringRow, Pair(translatedCount, totalCount))
        }

        // Emit progress update via SSE flow
        val flow = dora.languageFlows.getOrPut(project.slug + "-" + languageSlug) { MutableStateFlow(TranslationProgress(0, 0)) }
        flow.emit(TranslationProgress(result.counts.first, result.counts.second))

        call.respondHtmlFragment {
            batchEntry(
                project,
                uniqueId,
                languageSlug,
                stringId,
                result.sourceStringRow[SourceStrings.context],
                result.sourceStringRow[SourceStrings.transformers],
                result.sourceStringRow[SourceStrings.text],
                null, // no translation anymore
                false, // not translated
                null, // no approvedBy
                false
            )

            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    "Tradução excluída!"
                )
            )
        }
    }
}
