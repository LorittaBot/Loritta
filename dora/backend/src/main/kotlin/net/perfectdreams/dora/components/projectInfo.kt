package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.textArea
import kotlinx.html.textInput
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.components.ButtonStyle
import net.perfectdreams.luna.components.sectionEntry

fun FlowContent.projectInfoForm(
    project: Project,
    description: String,
    repositoryUrl: String,
    iconUrl: String?,
    languagesFolder: String,
    sourceBranch: String,
    sourceLanguageName: String,
    sourceLanguageId: String) {
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
                    value = project.name
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
                    value = project.slug
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
                    +description
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
                    value = repositoryUrl
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
                    value = iconUrl ?: ""
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
                    value = languagesFolder
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
                    value = sourceBranch
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
                    value = sourceLanguageName
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
                    value = sourceLanguageId
                }
            }

            fieldWrapper {
                discordButton(ButtonStyle.PRIMARY) {
                    attributes["bliss-put"] = "/projects/${project.slug}"
                    attributes["bliss-include-json"] = "#form-wrapper"
                    attributes["bliss-swap:200"] = ".entries (innerHTML) -> .entries (innerHTML), #body (innerHTML) -> #right-sidebar-contents (innerHTML)"

                    text("Salvar")
                }
            }
        }
    }
}
