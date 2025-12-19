package net.perfectdreams.dora.routes

import io.ktor.server.application.*
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.PermissionLevel
import net.perfectdreams.dora.components.dashboardBase
import net.perfectdreams.dora.components.homeLeftSidebarEntries
import net.perfectdreams.dora.components.projects
import net.perfectdreams.dora.tables.Projects
import net.perfectdreams.dora.utils.respondHtml
import net.perfectdreams.luna.components.sectionEntry
import org.jetbrains.exposed.sql.selectAll

class HomeRoute(val dora: DoraBackend) : RequiresUserAuthDashboardLocalizedRoute(dora, "/") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, session: DoraUserSession, userPermissionLevel: PermissionLevel) {
        val projects = dora.pudding.transaction {
            Projects.selectAll().toList()
        }

        call.respondHtml {
            dashboardBase(
                "Dora",
                {
                    homeLeftSidebarEntries()
                },
            ) {
                projects(projects)
            }
        }
    }
}