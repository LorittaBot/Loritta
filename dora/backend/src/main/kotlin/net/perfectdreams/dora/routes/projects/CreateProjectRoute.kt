package net.perfectdreams.dora.routes.projects

import io.ktor.server.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.textArea
import kotlinx.html.textInput
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.DoraUserSession
import net.perfectdreams.dora.PermissionLevel
import net.perfectdreams.dora.components.ButtonStyle
import net.perfectdreams.dora.components.dashboardBase
import net.perfectdreams.dora.components.discordButton
import net.perfectdreams.dora.components.fieldInformationBlock
import net.perfectdreams.dora.components.fieldTitle
import net.perfectdreams.dora.components.fieldWrapper
import net.perfectdreams.dora.components.fieldWrappers
import net.perfectdreams.dora.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.dora.utils.respondHtml
import net.perfectdreams.dora.utils.respondHtmlFragment
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.luna.toasts.blissShowToast
import net.perfectdreams.luna.toasts.createEmbeddedToast
import net.perfectdreams.luna.components.sectionEntry

class CreateProjectRoute(website: DoraBackend) : RequiresUserAuthDashboardLocalizedRoute(website, "/projects/create") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, session: DoraUserSession, userPermissionLevel: PermissionLevel) {
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
        call.respondHtml {
            dashboardBase(
                "Dora",
                {
                    sectionEntry("/projects", true) {
                        text("Projetos")
                    }
                },
            ) {
                div {
                    id = "form-wrapper"

                    fieldWrappers {
                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("Nome do Projeto")
                                }
                            }

                            textInput {
                                name = "name"
                            }
                        }

                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("Slug do Projeto")
                                }
                            }

                            textInput {
                                name = "slug"
                            }
                        }

                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("Descrição")
                                }
                            }

                            textArea {
                                name = "description"
                            }
                        }

                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("URL do Repositório")
                                }
                            }

                            textInput {
                                name = "repositoryUrl"
                                placeholder = "https://github.com/LorittaBot/Loritta"
                            }
                        }

                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("URL do Ícone do Projeto (opcional)")
                                }
                            }

                            textInput {
                                name = "iconUrl"
                                placeholder = "https://example.com/icon.png"
                            }
                        }

                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("Pasta de Idiomas")
                                }
                            }

                            textInput {
                                name = "languagesFolder"
                                placeholder = "resources/languages/"
                            }
                        }

                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("Branch Source")
                                }
                            }

                            textInput {
                                name = "sourceBranch"
                                placeholder = "main"
                            }
                        }

                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("Idioma Original (Nome)")
                                }
                            }

                            textInput {
                                name = "sourceLanguageName"
                                placeholder = "Português (Brasil)"
                            }
                        }

                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text("Idioma Original (ID)")
                                }
                            }

                            textInput {
                                name = "sourceLanguageId"
                                placeholder = "pt"
                            }
                        }

                        fieldWrapper {
                            discordButton(ButtonStyle.PRIMARY) {
                                attributes["bliss-post"] = "/projects"
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