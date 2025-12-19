package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.server.util.getOrFail
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.components.translatorsOverview
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.CachedDiscordUserIdentifications
import net.perfectdreams.dora.tables.ProjectUserPermissions
import net.perfectdreams.dora.tables.Users
import net.perfectdreams.dora.utils.respondHtmlFragment
import net.perfectdreams.luna.modals.blissCloseModal
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.blissShowToast
import net.perfectdreams.luna.toasts.createEmbeddedToast
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll

class DeleteProjectTranslatorRoute(website: DoraBackend) : RequiresProjectAuthDashboardRoute(website, "/translators/{userId}") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        // Only project OWNERs can delete translators
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

        val userId = call.parameters.getOrFail("userId").toLong()

        val result = website.pudding.transaction {
            val permissionToBeDeleted = ProjectUserPermissions.selectAll()
                .where {
                    (ProjectUserPermissions.project eq project.id) and (ProjectUserPermissions.user eq userId)
                }
                .first()

            if (permissionToBeDeleted[ProjectUserPermissions.permissionLevel] == ProjectPermissionLevel.OWNER)
                return@transaction Result.CannotDeleteOwner

            // Delete the specific permission entry for this project and user
            ProjectUserPermissions.deleteWhere {
                ProjectUserPermissions.id eq permissionToBeDeleted[ProjectUserPermissions.id]
            }

            // Re-query updated permissions list
            val permissions = ProjectUserPermissions
                .leftJoin(Users, { ProjectUserPermissions.user }, { Users.id })
                .leftJoin(CachedDiscordUserIdentifications, { Users.id }, { CachedDiscordUserIdentifications.id })
                .selectAll()
                .where { ProjectUserPermissions.project eq project.id }
                .toList()

            return@transaction Result.Success(permissions)
        }

        when (result) {
            Result.CannotDeleteOwner -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Você não pode remover o dono do projeto!"
                        )
                    )
                }
            }

            is Result.Success -> {
                call.respondHtmlFragment {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            "Tradutor removido com sucesso!"
                        )
                    )

                    blissCloseModal()

                    translatorsOverview(
                        project,
                        result.updatedPermissions
                    )
                }
            }
        }
    }

    sealed class Result {
        data class Success(val updatedPermissions: List<ResultRow>) : Result()
        data object CannotDeleteOwner : Result()
    }
}
