package net.perfectdreams.dora.routes.projects.languages

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.util.getOrFail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.style
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.blissShowToast
import net.perfectdreams.luna.toasts.createEmbeddedToast
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.Translator
import net.perfectdreams.dora.components.batchEntry
import net.perfectdreams.dora.components.languageProgressBar
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.CachedDiscordUserIdentifications
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.MachineTranslatedStrings
import net.perfectdreams.dora.tables.SourceStrings
import net.perfectdreams.dora.tables.TranslationsStrings
import net.perfectdreams.dora.tables.Users
import net.perfectdreams.dora.utils.TranslationProgress
import net.perfectdreams.dora.utils.respondHtmlFragment
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import java.time.OffsetDateTime

class PutTableEntryRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/languages/{languageSlug}/table/{stringId}/{uniqueId}") {
    @Serializable
    data class SaveBatchEntryRoute(
        val translatedText: String
    )

    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        val request = Json.decodeFromString<SaveBatchEntryRoute>(call.receiveText())

        val uniqueId = call.parameters.getOrFail("uniqueId")
        val languageSlug = call.parameters.getOrFail("languageSlug")
        val stringId = call.parameters.getOrFail("stringId")

        val result = dora.pudding.transaction {
            val language = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.languageId eq languageSlug and (LanguageTargets.project eq project.id)
                }
                .first()

            val sourceStringRow = SourceStrings
                .leftJoin(TranslationsStrings, { SourceStrings.id }, { TranslationsStrings.sourceString })
                {
                    TranslationsStrings.language eq language[LanguageTargets.id]
                }
                .leftJoin(MachineTranslatedStrings, { SourceStrings.id }, { MachineTranslatedStrings.sourceString })
                {
                    MachineTranslatedStrings.language eq language[LanguageTargets.id]
                }
                .selectAll()
                .where {
                    SourceStrings.key eq stringId and (SourceStrings.project eq project.id)
                }
                .first()
            
            if (sourceStringRow.getOrNull(TranslationsStrings.text) != null && sourceStringRow[TranslationsStrings.text] == request.translatedText)
                return@transaction Result.AlreadyApprovedSameText

            TranslationsStrings.deleteWhere {
                TranslationsStrings.language eq language[LanguageTargets.id] and (TranslationsStrings.sourceString eq sourceStringRow[SourceStrings.id])
            }

            TranslationsStrings.insert {
                it[TranslationsStrings.language] = language[LanguageTargets.id]
                it[TranslationsStrings.sourceString] = sourceStringRow[SourceStrings.id]
                it[TranslationsStrings.text] = request.translatedText
                it[TranslationsStrings.translatedBy] = session.userId
                it[TranslationsStrings.translatedAt] = OffsetDateTime.now()
            }

            val translatedBy = Users
                .innerJoin(CachedDiscordUserIdentifications, { Users.id }, { CachedDiscordUserIdentifications.id })
                .selectAll()
                .where {
                    Users.id eq session.userId
                }
                .first()
                .let {
                    Translator(
                        it[Users.id].value,
                        it[CachedDiscordUserIdentifications.id].value,
                        it[CachedDiscordUserIdentifications.username],
                        it[CachedDiscordUserIdentifications.avatarId]
                    )
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

            return@transaction Result.Success(sourceStringRow, translatedBy, Pair(translatedCount, totalCount))
        }

        when (result) {
            is Result.Success -> {
                val flow = dora.languageFlows.getOrPut(project.slug + "-" + languageSlug) { MutableStateFlow(TranslationProgress(0, 0)) }
                flow.emit(TranslationProgress(result.counts.first, result.counts.second))

                call.respondHtmlFragment {
                    batchEntry(
                        project,
                        uniqueId,
                        languageSlug,
                        stringId,
                        result.sourceStringRow[SourceStrings.context],
                        result.sourceStringRow[SourceStrings.text],
                        result.sourceStringRow.getOrNull(MachineTranslatedStrings.text),
                        request.translatedText,
                        true,
                        result.translatedBy,
                        false
                    )

                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.INFO,
                            "Salvo!"
                        )
                    )
                }
            }
            Result.AlreadyApprovedSameText -> {
                call.respondHtmlFragment(status = HttpStatusCode.Conflict) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "String já foi aprovada com o mesmo conteúdo!"
                        )
                    )
                }
            }
        }
    }
    
    sealed class Result {
        data class Success(
            val sourceStringRow: ResultRow,
            val translatedBy: Translator,
            val counts: Pair<Int, Int>
        ) : Result()
        data object AlreadyApprovedSameText : Result()
    }
}