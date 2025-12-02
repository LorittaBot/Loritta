package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.fileInput
import kotlinx.html.id
import kotlinx.html.style
import kotlinx.html.textArea
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
            div {
                text("Enviando como ${userInfo.name} $bannedUserId")
            }

            div {
                text("Atualmente você está banido por: ${banState.reason}")
            }
        }

        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text("Quais são os IDs das suas contas do Discord?")
                }

                fieldDescription {
                    text("Preencha com o ID de TODAS as suas contas do Discord")
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
                }
            }

            textArea {
                id = "xd"
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
                attributes["bliss-swap:200"] = "body (innerHTML) -> #appeal-form (innerHTML)"
                attributes["bliss-disable-when"] = "[name=whatDidYouDo] == blank || [name='whyDidYouBreakThem'] == blank || [name='whyShouldYouBeUnbanned'] == blank"
                text("Enviar")
            }
        }
    }
}