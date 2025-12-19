package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.ApplicationCall
import kotlinx.html.div
import kotlinx.html.style
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectDashboardSection
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.components.dashboardBase
import net.perfectdreams.dora.components.projectLeftSidebarEntries
import net.perfectdreams.dora.components.translatorsOverview
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.CachedDiscordUserIdentifications
import net.perfectdreams.dora.tables.ProjectUserPermissions
import net.perfectdreams.dora.tables.Users
import net.perfectdreams.dora.utils.respondHtml
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll

class ProjectTranslatorsRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/translators") {
    override suspend fun onAuthenticatedProjectRequest(
        call: ApplicationCall,
        session: DoraUserSession,
        project: Project,
        projectPermissionLevel: ProjectPermissionLevel
    ) {
        val permissions: List<ResultRow> = dora.pudding.transaction {
            ProjectUserPermissions
                .leftJoin(Users, { ProjectUserPermissions.user }, { Users.id })
                .leftJoin(CachedDiscordUserIdentifications, { Users.id }, { CachedDiscordUserIdentifications.id })
                .selectAll()
                .where { ProjectUserPermissions.project eq project.id }
                .toList()
        }

        call.respondHtml {
            dashboardBase(
                "Dora",
                {
                    projectLeftSidebarEntries(
                        project,
                        ProjectDashboardSection.TRANSLATORS
                    )
                }
            ) {
                translatorsOverview(project, permissions)
            }
        }
    }
}
