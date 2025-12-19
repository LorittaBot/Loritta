package net.perfectdreams.dora.routes.projects

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.PermissionLevel
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectPermissionLevel
import net.perfectdreams.dora.components.translatorsOverview
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.CachedDiscordUserIdentifications
import net.perfectdreams.dora.tables.ProjectUserPermissions
import net.perfectdreams.dora.tables.Users
import net.perfectdreams.dora.utils.respondHtmlFragment
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.blissShowToast
import net.perfectdreams.luna.toasts.createEmbeddedToast
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PostCreateProjectTranslatorRoute(website: DoraBackend) : RequiresProjectAuthDashboardRoute(website, "/translators") {
    @Serializable
    data class CreateTranslatorRequest(
        val discordId: Long,
        val permissionLevel: ProjectPermissionLevel
    )

    override suspend fun onAuthenticatedProjectRequest(
        call: ApplicationCall,
        session: DoraUserSession,
        project: Project,
        projectPermissionLevel: ProjectPermissionLevel
    ) {
        // Only project OWNERs can add translators
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
        val request = Json.decodeFromString<CreateTranslatorRequest>(call.receiveText())

        val targetUserId = request.discordId
        val chosenLevel = request.permissionLevel

        if (chosenLevel != ProjectPermissionLevel.TRANSLATOR && chosenLevel != ProjectPermissionLevel.ADMIN)
            throw IllegalArgumentException("Invalid permission level!")

        val result = website.pudding.transaction {
            // Ensure user exists in Users table
            val exists = Users.selectAll().where { Users.id eq targetUserId }.firstOrNull() != null
            if (!exists) {
                Users.insert {
                    it[Users.id] = targetUserId
                    it[Users.permissionLevel] = PermissionLevel.USER
                }
            }

            // Insert permission if not exists
            val alreadyHas = ProjectUserPermissions.selectAll().where {
                (ProjectUserPermissions.project eq project.id) and (ProjectUserPermissions.user eq targetUserId)
            }.firstOrNull() != null

            if (alreadyHas)
                return@transaction Result.UserAlreadyExists

            ProjectUserPermissions.insert {
                it[ProjectUserPermissions.project] = project.id
                it[ProjectUserPermissions.user] = targetUserId
                it[ProjectUserPermissions.permissionLevel] = chosenLevel
            }

            val permissions = ProjectUserPermissions
                .leftJoin(Users, { ProjectUserPermissions.user }, { Users.id })
                .leftJoin(CachedDiscordUserIdentifications, { Users.id }, { CachedDiscordUserIdentifications.id })
                .selectAll()
                .where { ProjectUserPermissions.project eq project.id }
                .toList()

            return@transaction Result.Success(permissions)
        }

        when (result) {
            is Result.Success -> {
                call.response.header("Bliss-Push-Url", "/projects/${project.slug}/translators")

                call.respondHtmlFragment {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.SUCCESS,
                            "Tradutor adicionado com sucesso!"
                        )
                    )

                    translatorsOverview(project, result.permissions)
                }
            }
            Result.UserAlreadyExists -> {
                call.respondHtmlFragment(status = HttpStatusCode.Conflict) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Não foi possível adicionar a permissão!"
                        )
                    )
                }
            }
        }
    }

    sealed class Result {
        data class Success(val permissions: List<ResultRow>) : Result()
        data object UserAlreadyExists : Result()
    }
}
