package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.util.getOrFail
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.blissShowToast
import net.perfectdreams.luna.toasts.createEmbeddedToast
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.Projects
import net.perfectdreams.dora.utils.respondHtmlFragment
import org.jetbrains.exposed.sql.selectAll

class PostPullProjectRoute(val dora: DoraBackend) : RequiresProjectAuthDashboardRoute(dora, "/pull") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        // Only project OWNERs can pull
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
        val projectSlug = call.parameters.getOrFail("projectSlug")

        val project = dora.pudding.transaction {
            val project = Projects.selectAll()
                .where {
                    Projects.slug eq projectSlug
                }
                .first()

            project
        }

        dora.pullTranslations(project)

        call.respondHtmlFragment {
            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    "Alterações recebidas!"
                )
            )
        }
    }
}