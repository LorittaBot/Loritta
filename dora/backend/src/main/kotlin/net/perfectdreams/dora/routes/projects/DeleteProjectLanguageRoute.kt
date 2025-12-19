package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.*
import io.ktor.server.util.*
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.components.projectOverview
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.MachineTranslatedStrings
import net.perfectdreams.dora.tables.TranslationsStrings
import net.perfectdreams.dora.utils.respondHtmlFragment
import net.perfectdreams.luna.modals.blissCloseModal
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.blissShowToast
import net.perfectdreams.luna.toasts.createEmbeddedToast
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

class DeleteProjectLanguageRoute(website: DoraBackend) : RequiresProjectAuthDashboardRoute(website, "/languages/{languageSlug}") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        val languageSlug = call.parameters.getOrFail("languageSlug")

        val languageTargets = website.pudding.transaction {
            val languageTarget = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.languageId eq languageSlug and (LanguageTargets.project eq project.id)
                }
                .first()

            MachineTranslatedStrings.deleteWhere {
                MachineTranslatedStrings.language eq languageTarget[LanguageTargets.id]
            }

            TranslationsStrings.deleteWhere {
                TranslationsStrings.language eq languageTarget[LanguageTargets.id]
            }

            LanguageTargets.deleteWhere {
                LanguageTargets.id eq languageTarget[LanguageTargets.id]
            }

            LanguageTargets.selectAll()
                .where { LanguageTargets.project eq project.id }
                .toList()
        }

        call.respondHtmlFragment {
            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    "Idioma excluido com sucesso!"
                )
            )

            blissCloseModal()

            projectOverview(
                project,
                languageTargets
            )
        }
    }
}