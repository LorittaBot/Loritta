package net.perfectdreams.dora.routes.projects.languages

import io.ktor.server.application.ApplicationCall
import io.ktor.server.util.getOrFail
import kotlinx.html.div
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.Translator
import net.perfectdreams.dora.components.batchEntry
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.dora.tables.CachedDiscordUserIdentifications
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.MachineTranslatedStrings
import net.perfectdreams.dora.tables.SourceStrings
import net.perfectdreams.dora.tables.TranslationsStrings
import net.perfectdreams.dora.tables.Users
import net.perfectdreams.dora.utils.respondHtmlFragment
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll

class TableEntryEditorRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/languages/{languageSlug}/table/{stringId}/{uniqueId}/editor") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        val uniqueId = call.parameters.getOrFail("uniqueId")
        val languageSlug = call.parameters.getOrFail("languageSlug")
        val stringId = call.parameters.getOrFail("stringId")

        val stringRow = dora.pudding.transaction {
            val language = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.languageId eq languageSlug and (LanguageTargets.project eq project.id)
                }
                .first()

            SourceStrings
                .leftJoin(TranslationsStrings, { SourceStrings.id }, { TranslationsStrings.sourceString })
                {
                    TranslationsStrings.language eq language[LanguageTargets.id]
                }
                .leftJoin(Users, { TranslationsStrings.translatedBy }, { Users.id })
                .leftJoin(CachedDiscordUserIdentifications, { Users.id }, { CachedDiscordUserIdentifications.id })
                .leftJoin(MachineTranslatedStrings, { SourceStrings.id }, { MachineTranslatedStrings.sourceString })
                {
                    MachineTranslatedStrings.language eq language[LanguageTargets.id]
                }
                .selectAll()
                .where {
                    SourceStrings.project eq project.id and (SourceStrings.key eq stringId)
                }
                .first()
        }

        call.respondHtmlFragment {
            div {
                batchEntry(
                    project,
                    uniqueId,
                    languageSlug,
                    stringRow[SourceStrings.key],
                    stringRow[SourceStrings.context],
                    stringRow[SourceStrings.text],
                    stringRow.getOrNull(MachineTranslatedStrings.text),
                    stringRow.getOrNull(TranslationsStrings.text),
                    stringRow.getOrNull(TranslationsStrings.text) != null,
                    if (stringRow.getOrNull(TranslationsStrings.id) != null) {
                        Translator(
                            stringRow[Users.id].value,
                            stringRow.getOrNull(CachedDiscordUserIdentifications.id)?.value ?: 0L,
                            stringRow.getOrNull(CachedDiscordUserIdentifications.username) ?: "Unknown",
                            stringRow[CachedDiscordUserIdentifications.avatarId]
                        )
                    } else null,
                    true
                )
            }
        }
    }
}