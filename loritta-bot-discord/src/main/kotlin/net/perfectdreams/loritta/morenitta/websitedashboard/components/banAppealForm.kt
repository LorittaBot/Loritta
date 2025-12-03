package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.fileInput
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.textArea
import kotlinx.html.ul
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.serializable.UserBannedState

fun FlowContent.banAppealForm(
    i18nContext: I18nContext,
    bannedUserId: Long,
    userInfo: CachedUserInfo,
    banState: UserBannedState
) {
    fieldWrappers {
        fieldWrapper {
            div(classes = "simple-image-with-text-header") {
                img(src = userInfo.effectiveAvatarUrl, alt = "Avatar de ${userInfo.name}", classes = "round-corners")

                h1 {
                    text("Apelo de ${userInfo.globalName ?: userInfo.name}")
                }
            }

            ul {
                li {
                    text("Motivo do Ban: ${banState.reason}")
                }
            }
        }

        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text("Quais são os IDs das suas contas do Discord?")
                }

                fieldDescription {
                    div {
                        text("Preencha com o ID de TODAS as suas contas do Discord, um ID por linha. Você não precisa preencher se você só está banido na conta ${userInfo.name} (${userInfo.id}).")
                    }

                    div {
                        text("Nós precisamos saber as suas contas pois, se você for desbanido enquanto você tiver outras contas banidas, você pode acabar sendo banido novamente!")
                    }

                    div {
                        text("Não sabe copiar IDs? Então ")
                        a(href = "https://support.discord.com/hc/pt-br/articles/206346498-Onde-posso-encontrar-minhas-IDs-de-usu%C3%A1rio-servidor-e-mensagem", target = "_blank") {
                            text("clique aqui")
                        }
                        text("!")
                    }
                }
            }

            textArea {
                name = "accountIdsRaw"
                minLength = "0"
                maxLength = "1000"

                attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/form/account-ids"
                attributes["bliss-swap:200"] = "body (innerHTML) -> #account-ids-output (innerHTML)"
                attributes["bliss-include-json"] = "[name='accountIdsRaw']"
                attributes["bliss-trigger"] = "input"
            }

            div {
                style = "display: flex; gap: 8px; flex-direction: column;"
                id = "account-ids-output"
            }
        }

        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text("O que você fez para ser banido?")

                    span {
                        style = "color: var(--loritta-red); margin-left: 4px;"
                        text("*")
                    }
                }

                fieldDescription {
                    text("Nós sabemos o motivo que você foi banido, mas a gente quer que você seja honesto e descreva o que você fez para ser banido.")
                }
            }

            textArea {
                attributes["loritta-ban-appeal-attribute"] = "true"
                name = "whatDidYouDo"
                minLength = "0"
                maxLength = "1000"
            }

            characterCounter("[name='whatDidYouDo']")
        }

        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text("Por que você quebrou as regras da Loritta?")

                    span {
                        style = "color: var(--loritta-red); margin-left: 4px;"
                        text("*")
                    }
                }

                fieldDescription {
                    text("Se você quebrou as regras, tem algum motivo por trás. Nós queremos saber o que te levou a quebrar as regras da Loritta ao invés de respeitá-las.")
                }
            }

            textArea {
                attributes["loritta-ban-appeal-attribute"] = "true"
                name = "whyDidYouBreakThem"
                minLength = "0"
                maxLength = "1000"
            }

            characterCounter("[name='whyDidYouBreakThem']")
        }

        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text("Por que você deveria ser desbanido?")

                    span {
                        style = "color: var(--loritta-red); margin-left: 4px;"
                        text("*")
                    }
                }

                fieldDescription {
                    text("Para a gente te dar uma segunda chance, nós precisamos acreditar que você não irá quebrar as regras da Loritta novamente.")
                }
            }

            textArea {
                attributes["loritta-ban-appeal-attribute"] = "true"
                name = "whyShouldYouBeUnbanned"
                minLength = "0"
                maxLength = "1000"
            }

            characterCounter("[name='whyShouldYouBeUnbanned']")
        }

        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text("Deseja comentar mais alguma coisa sobre o seu ban?")
                }

                fieldDescription {
                    text("Mande um recado para a equipe da Loritta!")
                }
            }

            textArea {
                attributes["loritta-ban-appeal-attribute"] = "true"
                name = "additionalComments"
                minLength = "0"
                maxLength = "1000"
            }

            characterCounter("[name='additionalComments']")
        }

        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text("Imagens relacionadas com o banimento")
                }

                fieldDescription {
                    div {
                        text("Você pode incluir aqui imagens relacionadas com o banimento, como provas que mostrem a sua inocência.")
                    }

                    div {
                        text("Se você tem um gato, você pode enviar uma foto dele!")
                    }

                    div {
                        text("Você pode enviar até 10 imagens.")
                    }
                }
            }

            fileInput {
                attributes["loritta-ban-appeal-attribute"] = "true"
                multiple = true
                accept = "image/png, image/jpeg"
                name = "files"
            }
        }

        fieldWrapper {
            discordButton(ButtonStyle.PRIMARY) {
                style = "margin-left: auto;"

                attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/form"
                attributes["bliss-include-json"] = "[loritta-ban-appeal-attribute='true']"
                attributes["bliss-vals-json"] = buildJsonObject {
                    put("userId", bannedUserId)
                }.toString()
                attributes["bliss-swap:200"] = "body (innerHTML) -> #ban-appeal-content (innerHTML)"
                attributes["bliss-disable-when"] = "[name=whatDidYouDo] == blank || [name='whyDidYouBreakThem'] == blank || [name='whyShouldYouBeUnbanned'] == blank"
                text("Enviar")
            }
        }
    }
}