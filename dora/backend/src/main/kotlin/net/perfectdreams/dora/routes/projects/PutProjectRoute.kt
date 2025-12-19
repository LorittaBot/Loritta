package net.perfectdreams.dora.routes.projects

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectDashboardSection
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.components.ButtonStyle
import net.perfectdreams.dora.components.projectInfoForm
import net.perfectdreams.dora.components.projectLeftSidebarEntries
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.Projects
import net.perfectdreams.dora.utils.respondHtmlFragment
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.blissShowToast
import net.perfectdreams.luna.toasts.createEmbeddedToast
import org.jetbrains.exposed.sql.update

class PutProjectRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "") {
    @Serializable
    data class UpdateProjectRequest(
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

    override suspend fun onAuthenticatedProjectRequest(
        call: ApplicationCall,
        session: DoraUserSession,
        project: Project,
        projectPermissionLevel: ProjectPermissionLevel
    ) {
        // Only project OWNERs can edit the project info
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

        val request = Json.decodeFromString<UpdateProjectRequest>(call.receiveText())

        dora.pudding.transaction {
            Projects.update({ Projects.id eq project.id }) {
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
        }

        // If slug changed, push URL to the new one
        val newSlug = request.slug
        call.response.header("Bliss-Push-Url", "/projects/$newSlug/info")

        val updatedProject = project.copy(
            slug = request.slug,
            name = request.name,
            iconUrl = request.iconUrl
        )

        call.respondHtmlFragment {
            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    "Projeto atualizado com sucesso!"
                )
            )

            // Update both left entries and right body
            div(classes = "entries") {
                projectLeftSidebarEntries(updatedProject, ProjectDashboardSection.INFO)
            }

            div {
                id = "body"

                projectInfoForm(
                    updatedProject,
                    request.description,
                    request.repositoryUrl,
                    request.iconUrl,
                    request.languagesFolder,
                    request.sourceBranch,
                    request.sourceLanguageName,
                    request.sourceLanguageId
                )
            }
        }
    }
}
