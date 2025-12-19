package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.*
import kotlinx.html.*
import net.perfectdreams.dora.*
import net.perfectdreams.dora.components.*
import net.perfectdreams.dora.routes.RequiresProjectAuthDashboardRoute
import net.perfectdreams.dora.utils.respondHtml

class CreateProjectTranslatorRoute(website: DoraBackend) : RequiresProjectAuthDashboardRoute(website, "/translators/create") {
    override suspend fun onAuthenticatedProjectRequest(call: ApplicationCall, session: DoraUserSession, project: Project, projectPermissionLevel: ProjectPermissionLevel) {
        call.respondHtml {
            dashboardBase(
                "Dora",
                {
                    projectLeftSidebarEntries(project, ProjectDashboardSection.TRANSLATORS)
                }
            ) {
                div {
                    id = "form-wrapper"

                    fieldWrappers {
                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("Discord ID do Tradutor")
                                }
                            }

                            textInput {
                                placeholder = "297153970613387264"
                                name = "discordId"
                            }
                        }

                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("Nível de Permissão")
                                }
                            }

                            select {
                                name = "permissionLevel"

                                option {
                                    value = ProjectPermissionLevel.TRANSLATOR.name
                                    text(ProjectPermissionLevel.TRANSLATOR.name)
                                }
                                option {
                                    value = ProjectPermissionLevel.ADMIN.name
                                    text(ProjectPermissionLevel.ADMIN.name)
                                }
                            }
                        }

                        fieldWrapper {
                            discordButton(ButtonStyle.PRIMARY) {
                                attributes["bliss-post"] = "/projects/${project.slug}/translators"
                                attributes["bliss-include-json"] = "#form-wrapper"
                                attributes["bliss-swap:200"] = "body (innerHTML) -> #right-sidebar-contents (innerHTML)"

                                text("Adicionar Tradutor")
                            }
                        }
                    }
                }
            }
        }
    }
}
