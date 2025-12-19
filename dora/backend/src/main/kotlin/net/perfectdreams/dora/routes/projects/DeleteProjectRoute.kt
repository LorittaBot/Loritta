package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.header
import kotlinx.html.div
import kotlinx.html.id
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.blissShowToast
import net.perfectdreams.luna.toasts.createEmbeddedToast
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.components.homeLeftSidebarEntries
import net.perfectdreams.dora.components.partialSwapWithEntries
import net.perfectdreams.dora.components.projects
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.MachineTranslatedStrings
import net.perfectdreams.dora.tables.ProjectUserPermissions
import net.perfectdreams.dora.tables.Projects
import net.perfectdreams.dora.tables.SourceStrings
import net.perfectdreams.dora.tables.TranslationsStrings
import net.perfectdreams.dora.utils.respondHtmlFragment
import net.perfectdreams.luna.modals.blissCloseModal
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

class DeleteProjectRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        // Only project OWNERs can delete the project
        if (projectPermissionLevel != ProjectPermissionLevel.OWNER) {
            call.respondHtmlFragment(status = HttpStatusCode.Unauthorized) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Você não tem permissão para fazer isto!"
                    )
                )
            }
            return
        }
        val projects = dora.pudding.transaction {
            // Get all language target IDs for this project
            val languageTargetIds = LanguageTargets.selectAll()
                .where { LanguageTargets.project eq project.id }
                .map { it[LanguageTargets.id] }

            // Delete translations that reference language targets of this project
            TranslationsStrings.deleteWhere { TranslationsStrings.language inList languageTargetIds }

            // Delete machine translated strings that reference language targets of this project
            MachineTranslatedStrings.deleteWhere { MachineTranslatedStrings.language inList languageTargetIds }

            // Delete language targets
            LanguageTargets.deleteWhere { LanguageTargets.project eq project.id }

            // Delete source strings
            SourceStrings.deleteWhere { SourceStrings.project eq project.id }

            // Delete project user permissions
            ProjectUserPermissions.deleteWhere { ProjectUserPermissions.project eq project.id }

            // Finally, delete the project itself
            Projects.deleteWhere { Projects.id eq project.id }

            Projects.selectAll().toList()
        }

        call.response.header("Bliss-Push-Url", "/projects")

        call.respondHtmlFragment {
            blissCloseModal()

            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    "Projeto deletado com sucesso!"
                )
            )

            partialSwapWithEntries({
                homeLeftSidebarEntries()
            }) {
                projects(projects)
            }
        }
    }
}