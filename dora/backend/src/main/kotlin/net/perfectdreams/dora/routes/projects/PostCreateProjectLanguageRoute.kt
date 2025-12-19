package net.perfectdreams.dora.routes.projects

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.dora.*
import net.perfectdreams.dora.components.languageLeftSidebarEntries
import net.perfectdreams.dora.components.languageOverview
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.tables.SourceStrings
import net.perfectdreams.dora.tables.TranslationsStrings
import net.perfectdreams.dora.utils.respondHtmlFragment
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.blissShowToast
import net.perfectdreams.luna.toasts.createEmbeddedToast
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class PostCreateProjectLanguageRoute(website: DoraBackend) : RequiresProjectAuthDashboardRoute(website, "/languages") {
    @Serializable
    data class CreateProjectLanguageRequest(
        val id: String,
        val name: String
    )

    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        // Only project OWNERs can add languages
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
        val request = Json.decodeFromString<CreateProjectLanguageRequest>(call.receiveText())

        val (languageTarget, translatedStrings, sourceStrings) = website.pudding.transaction {
            LanguageTargets.insert {
                it[LanguageTargets.project] = project.id
                it[LanguageTargets.languageId] = request.id
                it[LanguageTargets.languageName] = request.name
            }

            val languageTarget = LanguageTargets.selectAll()
                .where {
                    LanguageTargets.languageId eq request.id and (LanguageTargets.project eq project.id)
                }
                .first()

            val translatedStrings = TranslationsStrings.selectAll()
                .where {
                    TranslationsStrings.language eq languageTarget[LanguageTargets.id]
                }
                .toList()

            val sourceStrings = SourceStrings.selectAll()
                .where {
                    SourceStrings.project eq project.id
                }
                .toList()

            Triple(languageTarget, translatedStrings, sourceStrings)
        }

        call.response.header("Bliss-Push-Url", "/projects/${project.slug}/languages/${languageTarget[LanguageTargets.languageId]}")

        call.respondHtmlFragment {
            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    "Linguagem criada com sucesso!"
                )
            )

            div(classes = "entries") {
                languageLeftSidebarEntries(
                    project,
                    languageTarget,
                    LanguageDashboardSection.OVERVIEW,
                    translatedStrings.size,
                    sourceStrings.size
                )
            }

            div {
                id = "body"

                languageOverview(
                    project,
                    languageTarget,
                    translatedStrings,
                    sourceStrings
                )
            }
        }
    }
}