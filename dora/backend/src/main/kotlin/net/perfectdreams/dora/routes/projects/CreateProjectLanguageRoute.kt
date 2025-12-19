package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.*
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.textInput
import net.perfectdreams.dora.*
import net.perfectdreams.dora.components.*
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.utils.respondHtml

class CreateProjectLanguageRoute(website: DoraBackend) : RequiresProjectAuthDashboardRoute(website, "/languages/create") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
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
                div {
                    id = "form-wrapper"

                    fieldWrappers {
                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("Idioma (ID)")
                                }
                            }

                            textInput {
                                placeholder = "en"
                                name = "id"
                            }
                        }

                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("Idioma (Nome)")
                                }
                            }

                            textInput {
                                placeholder = "English"
                                name = "name"
                            }
                        }

                        fieldWrapper {
                            discordButton(ButtonStyle.PRIMARY) {
                                attributes["bliss-post"] = "/projects/${project.slug}/languages"
                                attributes["bliss-include-json"] = "#form-wrapper"
                                attributes["bliss-swap:200"] = ".entries (innerHTML) -> .entries (innerHTML), #body (innerHTML) -> #right-sidebar-contents (innerHTML)"

                                text("Criar")
                            }
                        }
                    }
                }
            }
        }
    }
}