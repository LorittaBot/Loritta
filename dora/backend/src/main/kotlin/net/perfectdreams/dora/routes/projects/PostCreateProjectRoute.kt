package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.blissShowToast
import net.perfectdreams.luna.toasts.createEmbeddedToast
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.PermissionLevel
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectDashboardSection
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.components.projectLeftSidebarEntries
import net.perfectdreams.dora.components.projectOverview
import net.perfectdreams.dora.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.ProjectUserPermissions
import net.perfectdreams.dora.tables.Projects
import net.perfectdreams.dora.utils.respondHtmlFragment
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class PostCreateProjectRoute(website: DoraBackend) : RequiresUserAuthDashboardLocalizedRoute(website, "/projects") {
    @Serializable
    data class CreateProjectRequest(
        val name: String,
        val slug: String,
        val description: String,
        val repositoryUrl: String,
        val languagesFolder: String,
        val sourceLanguageId: String,
        val sourceLanguageName: String,
        val sourceBranch: String,
        val iconUrl: String? = null
    )

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, session: DoraUserSession, userPermissionLevel: PermissionLevel) {
        // Only global OWNERs can create projects
        if (userPermissionLevel != PermissionLevel.OWNER) {
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
        val request = Json.decodeFromString<CreateProjectRequest>(call.receiveText())

        val project = website.pudding.transaction {
            val project = Projects.insert {
                it[Projects.fancyName] = request.name
                it[Projects.slug] = request.slug
                it[Projects.description] = request.description
                it[Projects.repositoryUrl] = request.repositoryUrl
                it[Projects.sourceLanguageId] = request.sourceLanguageId
                it[Projects.sourceLanguageName] = request.sourceLanguageName
                it[Projects.languagesFolder] = request.languagesFolder
                it[Projects.sourceBranch] = request.sourceBranch
                it[Projects.iconUrl] = request.iconUrl
            }

            // When creating a project, we will add ourselves as one of the users
            ProjectUserPermissions.insert {
                it[ProjectUserPermissions.user] = session.userId
                it[ProjectUserPermissions.project] = project[Projects.id]
                it[ProjectUserPermissions.permissionLevel] = ProjectPermissionLevel.OWNER
            }

            Projects.selectAll()
                .where {
                    Projects.id eq project[Projects.id]
                }
                .first()
        }

        // Attempt to pull translations
        website.pullTranslations(project)

        call.response.header("Bliss-Push-Url", "/projects/${request.slug}")

        // This is very stupid because the variable name is kinda dumb
        val projectDAO = Project(
            project[Projects.id].value,
            project[Projects.slug],
            project[Projects.fancyName],
            project.getOrNull(Projects.iconUrl)
        )

        call.respondHtmlFragment {
            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    "Projeto criado com sucesso!"
                )
            )

            div(classes = "entries") {
                projectLeftSidebarEntries(
                    projectDAO,
                    ProjectDashboardSection.OVERVIEW
                )
            }

            div {
                id = "body"

                projectOverview(projectDAO, emptyList())
            }
        }
    }
}