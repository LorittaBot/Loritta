package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.luna.modals.EmbeddedModal
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets.ProfilePresetsUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.profilePresetsSection(i18nContext: I18nContext, profilePresets: List<ResultRow>) {
    div(classes = "hero-wrapper") {
        div(classes = "hero-text") {
            h1 {
                text(i18nContext.get(DashboardI18nKeysData.ProfilePresets.Title))
            }

            for (line in i18nContext.get(DashboardI18nKeysData.ProfilePresets.Description)) {
                p {
                    text(line)
                }
            }
        }
    }

    hr {}

    cardsWithHeader {
        cardHeader {
            cardHeaderInfo {
                cardHeaderTitle {
                    text(i18nContext.get(DashboardI18nKeysData.ProfilePresets.Title))
                }

                cardHeaderDescription {
                    text(i18nContext.get(DashboardI18nKeysData.ProfilePresets.Accounts(profilePresets.size)))
                }
            }

            discordButtonLink(ButtonStyle.PRIMARY, null) {
                if (profilePresets.size >= ProfilePresetsUtils.MAX_PROFILE_PRESETS) {
                    classes += "disabled"
                } else {
                    href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-presets/create"
                    swapRightSidebarContentsAttributes()
                }

                text(i18nContext.get(DashboardI18nKeysData.ProfilePresets.CreatePreset))
            }
        }

        if (profilePresets.isNotEmpty()) {
            div(classes = "loritta-items-wrapper") {
                for (preset in profilePresets.sortedBy { it[UserCreatedProfilePresets.name] }) {
                    div(classes = "shop-item-entry rarity-rare") {
                        openModalOnClick(
                            createEmbeddedModal(
                                preset[UserCreatedProfilePresets.name],
                                EmbeddedModal.Size.MEDIUM,
                                true,
                                {
                                    div(classes = "loritta-item-preview-wrapper") {
                                        div(classes = "canvas-preview-wrapper-wrapper") {
                                            div(classes = "canvas-preview-wrapper") {
                                                img(
                                                    classes = "canvas-preview-profile-design",
                                                    src = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-preview?type=${preset[UserCreatedProfilePresets.profileDesign]}&background=${preset[UserCreatedProfilePresets.background]}"
                                                ) {
                                                    style = "width: 400px; aspect-ratio: 4/3;"
                                                }
                                            }
                                        }
                                    }
                                },
                                listOf(
                                    {
                                        defaultModalCloseButton(i18nContext)
                                    },
                                    {
                                        discordButton(ButtonStyle.DANGER) {
                                            openModalOnClick(
                                                createEmbeddedConfirmDeletionModal(i18nContext) {
                                                    attributes["bliss-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-presets/${preset[UserCreatedProfilePresets.id]}"
                                                    attributes["bliss-swap:200"] = "body (innerHTML) -> #right-sidebar-contents (innerHTML)"
                                                }
                                            )

                                            text("Excluir")
                                        }
                                    },
                                    {
                                        discordButton(ButtonStyle.PRIMARY) {
                                            attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-presets/${preset[UserCreatedProfilePresets.id]}"

                                            text("Aplicar")
                                        }
                                    }
                                )
                            )
                        )

                        div {
                            style = "position: relative;"

                            div {
                                style = "overflow: hidden; line-height: 0;"

                                img {
                                    src = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-preview?type=${preset[UserCreatedProfilePresets.profileDesign]}&background=${preset[UserCreatedProfilePresets.background]}"

                                    // The aspect ratio makes the design not be wonky when the image is not loaded
                                    style = "width: 100%; height: auto; aspect-ratio: 4/3;"
                                }
                            }

                            div(classes = "item-entry-information rarity-rare") {
                                div(classes = "item-entry-title rarity-rare") {
                                    text(preset[UserCreatedProfilePresets.name])
                                }
                            }
                        }
                    }
                }
            }
        } else {
            emptySection(i18nContext)
        }
    }
}