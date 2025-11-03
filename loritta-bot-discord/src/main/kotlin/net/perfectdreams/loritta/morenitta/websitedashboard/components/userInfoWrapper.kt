package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.style
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import net.perfectdreams.loritta.serializable.ColorTheme

fun FlowContent.userInfoWrapper(
    i18nContext: I18nContext,
    session: UserSession
) {
    div(classes = "user-info-wrapper") {
        div(classes = "user-info") {
            val avatarUrl = session.getEffectiveAvatarUrl()

            img(src = avatarUrl) {
                width = "24"
                height = "24"
            }

            div(classes = "user-tag") {
                div(classes = "name") {
                    text(session.cachedUserIdentification.globalName ?: session.cachedUserIdentification.username)
                }

                div(classes = "discriminator") {
                    text("@${session.cachedUserIdentification.username}")
                }
            }

            discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                openModalOnClick(
                    createEmbeddedModal(
                        i18nContext.get(DashboardI18nKeysData.ThemeSelector.SelectATheme),
                        true,
                        {
                            div(classes = "theme-selector") {
                                div(classes = "theme-selector-lori") {
                                    div(classes = "theme-selector-lori-inner") {
                                        img(src = "https://stuff.loritta.website/loritta-matrix-choice-cookiluck.png")

                                        div(classes = "theme-option light") {
                                            text(i18nContext.get(DashboardI18nKeysData.ThemeSelector.LightTheme))
                                        }

                                        div(classes = "theme-option dark") {
                                            text(i18nContext.get(DashboardI18nKeysData.ThemeSelector.DarkTheme))
                                        }
                                    }
                                }

                                div(classes = "theme-selector-buttons") {
                                    discordButton(ButtonStyle.PRIMARY) {
                                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/theme"
                                        attributes["bliss-vals-json"] = buildJsonObject {
                                            put("theme", ColorTheme.LIGHT.name)
                                        }.toString()

                                        text(i18nContext.get(DashboardI18nKeysData.ThemeSelector.LightTheme))
                                    }

                                    discordButton(ButtonStyle.PRIMARY) {
                                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/theme"
                                        attributes["bliss-vals-json"] = buildJsonObject {
                                            put("theme", ColorTheme.DARK.name)
                                        }.toString()

                                        text(i18nContext.get(DashboardI18nKeysData.ThemeSelector.DarkTheme))
                                    }

                                    discordButton(ButtonStyle.PRIMARY) {
                                        style = "grid-row: 2; grid-column: -1 / 1;"
                                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/theme"
                                        attributes["bliss-vals-json"] = buildJsonObject {
                                            put("theme", ColorTheme.SYNC_WITH_SYSTEM.name)
                                        }.toString()

                                        text(i18nContext.get(DashboardI18nKeysData.ThemeSelector.SyncWithSystem))
                                    }
                                }
                            }
                        },
                        listOf {
                            defaultModalCloseButton(i18nContext)
                        }
                    )
                )

                text(i18nContext.get(DashboardI18nKeysData.ThemeSelector.ThemeButton))
            }
        }
    }
}