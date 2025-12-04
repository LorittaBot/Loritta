package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.datetime.toJavaInstant
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.fileInput
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.hr
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
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.serializable.UserBannedState
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

            div {
                div {
                    b {
                        text("Motivo do Ban:")
                    }
                    text(" ")
                    text(banState.reason)
                }

                div {
                    b {
                        text("Banido em:")
                    }
                    text(" ")

                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    val formattedDate = banState.bannedAt.toJavaInstant().atZone(Constants.LORITTA_TIMEZONE).format(formatter)

                    text(formattedDate + " (" + DateUtils.formatDiscordLikeRelativeDate(i18nContext, Instant.now(), banState.bannedAt.toJavaInstant()) + ")")
                }

                div {
                    b {
                        text("Expira:")
                    }
                    text(" ")

                    val expiresAt = banState.expiresAt
                    if (expiresAt != null) {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        val formattedDate = expiresAt.toJavaInstant().atZone(Constants.LORITTA_TIMEZONE).format(formatter)

                        text(formattedDate + " (" + DateUtils.formatDiscordLikeRelativeDate(i18nContext, Instant.now(), expiresAt.toJavaInstant()) + ")")

                    } else {
                        text("Nunca (Permanente)")
                    }
                }
            }

            h2 {
                text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.Rules.AppealRules))
            }

            div {
                style = "display: flex; gap: 24px; flex-direction: column;"

                div {
                    div {
                        style = "font-weight: bold;"
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.Rules.BeHonest.Title))
                    }
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.Rules.BeHonest.Description))
                }

                div {
                    div {
                        style = "font-weight: bold;"
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.Rules.BePatient.Title))
                    }
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.Rules.BePatient.Description))
                }

                div {
                    div {
                        style = "font-weight: bold;"
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.Rules.BeAuthentic.Title))
                    }
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.Rules.BeAuthentic.Description))
                }

                div {
                    div {
                        style = "font-weight: bold;"
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.Rules.BeRespectful.Title))
                    }
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.Rules.BeRespectful.Description))
                }

                div {
                    div {
                        style = "font-weight: bold;"
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.Rules.BeDetailed.Title))
                    }
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.Rules.BeDetailed.Description))
                }
            }

            div {
                style = "font-weight: bold; font-size: 24px;"
                text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.GoodLuck))
                text(" ")
                img(src = "https://stuff.loritta.website/emotes/lori-lick.gif", classes = "discord-inline-emoji") {}
            }
        }

        hr {}

        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.AccountIds.Title))
                }

                fieldDescription {
                    div {
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.AccountIds.Description1(userInfo.name, userInfo.id.toString())))
                    }

                    div {
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.AccountIds.Description2))
                    }

                    div {
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.AccountIds.Description3))
                    }

                    div {
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.AccountIds.Description4))
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
                attributes["bliss-vals-json"] = buildJsonObject {
                    put("formUserId", bannedUserId)
                }.toString()
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
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.WhatDidYouDo.Title))

                    span {
                        style = "color: var(--loritta-red); margin-left: 4px;"
                        text("*")
                    }
                }

                fieldDescription {
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.WhatDidYouDo.Description))
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
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.WhyDidYouBreakThem.Title))

                    span {
                        style = "color: var(--loritta-red); margin-left: 4px;"
                        text("*")
                    }
                }

                fieldDescription {
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.WhyDidYouBreakThem.Description))
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
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.WhyShouldYouBeUnbanned.Title))

                    span {
                        style = "color: var(--loritta-red); margin-left: 4px;"
                        text("*")
                    }
                }

                fieldDescription {
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.WhyShouldYouBeUnbanned.Description))
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
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.AdditionalComments.Title))
                }

                fieldDescription {
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.AdditionalComments.Description))
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
                    text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.BanImages.Title))
                }

                fieldDescription {
                    div {
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.BanImages.Description1))
                    }

                    div {
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.BanImages.Description2))
                    }

                    div {
                        text(i18nContext.get(I18nKeysData.Website.BanAppeals.AppealForm.BanImages.Description3))
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