package net.perfectdreams.dora.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.getOrFail
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.tables.ProjectUserPermissions
import net.perfectdreams.dora.tables.Projects
import net.perfectdreams.dora.tables.UserWebsiteSessions
import net.perfectdreams.dora.tables.Users
import net.perfectdreams.dora.utils.Base58
import net.perfectdreams.loritta.morenitta.websitedashboard.DiscordAuthenticationResult
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.time.ZoneOffset

abstract class RequiresProjectAuthDashboardRoute(website: DoraBackend, originalProjectPath: String) : RequiresUserAuthDashboardLocalizedRoute(website, "/projects/{projectSlug}$originalProjectPath") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, session: DoraUserSession, userPermissionLevel: net.perfectdreams.dora.PermissionLevel) {
        val projectSlug = call.parameters.getOrFail("projectSlug")

        val (project, projectPermissionLevel) = website.pudding.transaction {
            val project = Projects.selectAll()
                .where {
                    Projects.slug eq projectSlug
                }
                .first()

            val projectPermissionLevel = ProjectUserPermissions
                .innerJoin(Users)
                .selectAll()
                .where {
                    ProjectUserPermissions.project eq project[Projects.id] and (Users.id eq session.userId)
                }
                .firstOrNull()
                ?.get(ProjectUserPermissions.permissionLevel)

            return@transaction Pair(project, projectPermissionLevel)
        }

        if (projectPermissionLevel != null) {
            onAuthenticatedProjectRequest(
                call,
                session,
                Project(
                    project[Projects.id].value,
                    project[Projects.slug],
                    project[Projects.fancyName],
                    project.getOrNull(Projects.iconUrl)
                ),
                projectPermissionLevel
            )
        } else {
            onUnauthenticatedProjectRequest(
                call,
                session,
                Project(
                    project[Projects.id].value,
                    project[Projects.slug],
                    project[Projects.fancyName],
                    project.getOrNull(Projects.iconUrl)
                )
            )
        }
    }

    abstract suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel)

    suspend fun onUnauthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project) {
        call.respondText("Você não tem permissão para acessar este projeto!")
    }
}