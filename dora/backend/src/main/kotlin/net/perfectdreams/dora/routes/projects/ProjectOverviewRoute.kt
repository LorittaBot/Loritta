package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.*
import net.perfectdreams.dora.*
import net.perfectdreams.dora.components.dashboardBase
import net.perfectdreams.dora.components.projectLeftSidebarEntries
import net.perfectdreams.dora.components.projectOverview
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.utils.respondHtml
import org.jetbrains.exposed.sql.selectAll

class ProjectOverviewRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        val languageTargets = dora.pudding.transaction {
            val languageTargets = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.project eq project.id
                }
                .toList()

            languageTargets
        }

        call.respondHtml {
            dashboardBase(
                "Dora",
                {
                    projectLeftSidebarEntries(
                        project,
                        ProjectDashboardSection.OVERVIEW
                    )
                }
            ) {
                projectOverview(project, languageTargets)
            }
        }
    }
}