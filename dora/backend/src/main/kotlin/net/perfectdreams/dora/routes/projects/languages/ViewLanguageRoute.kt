package net.perfectdreams.dora.routes.projects.languages

import io.ktor.server.application.*
import io.ktor.server.util.*
import net.perfectdreams.dora.*
import net.perfectdreams.dora.components.dashboardBase
import net.perfectdreams.dora.components.languageLeftSidebarEntries
import net.perfectdreams.dora.components.languageOverview
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.SourceStrings
import net.perfectdreams.dora.tables.TranslationsStrings
import net.perfectdreams.dora.utils.respondHtml
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class ViewLanguageRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/languages/{languageSlug}") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        val languageSlug = call.parameters.getOrFail("languageSlug")

        val (languageTarget, translatedStrings, sourceStrings) = dora.pudding.transaction {
            val languageTarget = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.languageId eq languageSlug and (LanguageTargets.project eq project.id)
                }
                .first()

            val translatedStrings = TranslationsStrings.selectAll()
                .where {
                    TranslationsStrings.language eq languageTarget[LanguageTargets.id]
                }
                .toList()

            val sourceStrings = SourceStrings.selectAll()
                .where {
                    SourceStrings.project eq project.id
                }
                .toList()

            Triple(languageTarget, translatedStrings, sourceStrings)
        }

        call.respondHtml {
            dashboardBase(
                "Dora",
                {
                    languageLeftSidebarEntries(
                        project,
                        languageTarget,
                        LanguageDashboardSection.OVERVIEW,
                        translatedStrings.size,
                        sourceStrings.size
                    )
                }
            ) {
                languageOverview(
                    project,
                    languageTarget,
                    translatedStrings,
                    sourceStrings
                )
            }
        }
    }
}