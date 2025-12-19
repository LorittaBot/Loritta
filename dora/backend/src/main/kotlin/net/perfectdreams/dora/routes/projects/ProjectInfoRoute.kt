package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.ApplicationCall
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectDashboardSection
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.components.dashboardBase
import net.perfectdreams.dora.components.projectInfoForm
import net.perfectdreams.dora.components.projectLeftSidebarEntries
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.Projects
import net.perfectdreams.dora.utils.respondHtml
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll

class ProjectInfoRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/info") {
    override suspend fun onAuthenticatedProjectRequest(
        call: ApplicationCall,
        session: DoraUserSession,
        project: Project,
        projectPermissionLevel: ProjectPermissionLevel
    ) {
        val row = dora.pudding.transaction {
            Projects.selectAll()
                .where { Projects.id eq project.id }
                .first()
        }

        call.respondHtml {
            dashboardBase(
                "Dora",
                {
                    projectLeftSidebarEntries(project.copy(iconUrl = row.getOrNull(Projects.iconUrl)), ProjectDashboardSection.INFO)
                }
            ) {
                projectInfoForm(
                    project.copy(iconUrl = row.getOrNull(Projects.iconUrl)),
                    row[Projects.description],
                    row[Projects.repositoryUrl],
                    row.getOrNull(Projects.iconUrl),
                    row[Projects.languagesFolder],
                    row[Projects.sourceBranch],
                    row[Projects.sourceLanguageName],
                    row[Projects.sourceLanguageId]
                )
            }
        }
    }
}
