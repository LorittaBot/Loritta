package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick

fun FlowContent.userDashLeftSidebarEntries(
    lorittaBot: LorittaBot,
    i18nContext: I18nContext,
    userPremiumPlans: UserPremiumPlans,
    selectedUserSection: UserDashboardSection
) {
    a(classes = "entry loritta-logo", href = lorittaBot.config.loritta.website.url.removeSuffix("/") + "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/") {
        text("Loritta")
    }

    leftSidebarHr()

    aDashboardSidebarEntry(i18nContext, "/", "Seus Servidores", SVGIcons.House, selectedUserSection == UserDashboardSection.CHOOSE_YOUR_SERVER, false)
    aDashboardSidebarEntry(i18nContext, "/user-app", i18nContext.get(DashboardI18nKeysData.PocketLoritta.Title), SVGIcons.DiamondsFour, selectedUserSection == UserDashboardSection.POCKET_LORITTA, false)

    a(classes = "entry section-entry", href = "https://sparklypower.net/?utm_source=loritta&utm_medium=loritta-dashboard&utm_campaign=sparklylori&utm_content=user-profile-sidebar") {
        sectionEntryContent(
            "Servidor de Minecraft da Loritta",
            SVGIcons.Pickaxe,
            true
        )
    }

    leftSidebarHr()

    div(classes = "category") {
        text("Sonhos")
    }

    aDashboardSidebarEntry(i18nContext, "/sonhos-shop", i18nContext.get(DashboardI18nKeysData.SonhosShop.Title), SVGIcons.ShoppingCart, selectedUserSection == UserDashboardSection.SONHOS_SHOP, false)
    sectionEntry(href = lorittaBot.config.loritta.website.url.removeSuffix("/") + "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/daily", selected = false) {
        sectionEntryContent("Recompensa Diária", SVGIcons.Star, false)
    }

    leftSidebarHr()

    div(classes = "category") {
        text("Personalização")
    }

    aDashboardSidebarEntry(i18nContext, "/profiles", i18nContext.get(DashboardI18nKeysData.ProfileDesigns.Title), SVGIcons.IdentificationCard, selectedUserSection == UserDashboardSection.PROFILE_DESIGNS, false)
    aDashboardSidebarEntry(i18nContext, "/backgrounds", i18nContext.get(DashboardI18nKeysData.Backgrounds.Title), SVGIcons.Images, selectedUserSection == UserDashboardSection.PROFILE_BACKGROUND, false)
    aDashboardSidebarEntry(i18nContext, "/profile-presets", i18nContext.get(DashboardI18nKeysData.ProfilePresets.Title), SVGIcons.BoxArrowUp, selectedUserSection == UserDashboardSection.PROFILE_PRESETS, true)
    aDashboardSidebarEntry(i18nContext, "/daily-shop", i18nContext.get(DashboardI18nKeysData.DailyShop.Title), SVGIcons.ShoppingBag, selectedUserSection == UserDashboardSection.TRINKETS_SHOP, false)
    sectionEntry(selected = false) {
        openModalOnClick(
            createEmbeddedModal(
                i18nContext.get(DashboardI18nKeysData.LorittaSpawner.PocketLoritta),
                false,
                {
                    div {
                        style = "text-align: center;"

                        for (line in i18nContext.get(DashboardI18nKeysData.LorittaSpawner.DoYouWantSomeCompany)) {
                            p {
                                text(line)
                            }
                        }
                    }

                    div(classes = "loritta-spawner-wrapper") {
                        div(classes = "loritta-spawners") {
                            div(classes = "loritta-spawner") {
                                img(src = "https://stuff.loritta.website/pocket-loritta/lori-sprites/repouso.png") {
                                    width = "128"
                                }

                                discordButton(ButtonStyle.SUCCESS) {
                                    attributes["bliss-component"] = "loritta-shimeji-spawner"
                                    attributes["spawner-type"] = "LORITTA"

                                    text(i18nContext.get(DashboardI18nKeysData.LorittaSpawner.SpawnPlayer("Loritta")))
                                }
                            }

                            div(classes = "loritta-spawner") {
                                img(src = "https://stuff.loritta.website/pocket-loritta/pantufa-sprites/repouso.png") {
                                    width = "128"
                                }

                                discordButton(ButtonStyle.SUCCESS) {
                                    attributes["bliss-component"] = "loritta-shimeji-spawner"
                                    attributes["spawner-type"] = "PANTUFA"

                                    text(i18nContext.get(DashboardI18nKeysData.LorittaSpawner.SpawnPlayer("Pantufa")))
                                }
                            }

                            div(classes = "loritta-spawner") {
                                img(src = "https://stuff.loritta.website/pocket-loritta/gabriela-sprites/repouso.png") {
                                    width = "128"
                                }

                                discordButton(ButtonStyle.SUCCESS) {
                                    attributes["bliss-component"] = "loritta-shimeji-spawner"
                                    attributes["spawner-type"] = "GABRIELA"

                                    text(i18nContext.get(DashboardI18nKeysData.LorittaSpawner.SpawnPlayer("Gabriela")))
                                }
                            }
                        }

                        discordButton(ButtonStyle.DANGER) {
                            attributes["bliss-component"] = "loritta-shimeji-clear"

                            text(i18nContext.get(DashboardI18nKeysData.LorittaSpawner.CleanUp))
                        }
                    }

                    fieldWrappers {
                        fieldWrapper {
                            fieldInformation(i18nContext.get(DashboardI18nKeysData.LorittaSpawner.ActivityLevel.Title))

                            select {
                                attributes["bliss-component"] = "loritta-shimeji-activity-level, fancy-select-menu"
                                attributes["fancy-select-menu-chevron-svg"] = SVGIcons.CaretDown.html.toString()

                                option {
                                    label = i18nContext.get(DashboardI18nKeysData.LorittaSpawner.ActivityLevel.Types.Low)
                                    value = "LOW"
                                }

                                option {
                                    label = i18nContext.get(DashboardI18nKeysData.LorittaSpawner.ActivityLevel.Types.Medium)
                                    value = "MEDIUM"
                                }

                                option {
                                    label = i18nContext.get(DashboardI18nKeysData.LorittaSpawner.ActivityLevel.Types.High)
                                    value = "HIGH"
                                }
                            }
                        }
                    }
                },
                listOf {
                    discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                        attributes["bliss-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/loritta-spawner"
                        attributes["loritta-include-spawner-settings"] = "true"

                        text(i18nContext.get(I18nKeysData.Website.Dashboard.Modal.Close))
                    }
                }
            )
        )

        sectionEntryContent(
            i18nContext.get(DashboardI18nKeysData.LorittaSpawner.PocketLoritta),
            SVGIcons.Cat,
            false
        )
    }

    leftSidebarHr()

    div(classes = "category") {
        text("Miscelânea")
    }

    aDashboardSidebarEntry(i18nContext, "/ship-effects", i18nContext.get(DashboardI18nKeysData.ShipEffects.Title), SVGIcons.Heart, selectedUserSection == UserDashboardSection.SHIP_EFFECTS, false)
    aDashboardSidebarEntry(i18nContext, "/api-keys", i18nContext.get(DashboardI18nKeysData.ApiKeys.Title), SVGIcons.Code, selectedUserSection == UserDashboardSection.API_KEYS, false)
    sectionEntry(href = lorittaBot.config.loritta.website.url.removeSuffix("/") + "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guidelines", selected = false) {
        sectionEntryContent("Diretrizes da Comunidade", SVGIcons.Asterisk, false)
    }

    leftSidebarHr()

    sectionEntry(selected = false) {
        style = "color: var(--loritta-red);"
        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/logout"

        div(classes = "section-icon") {
            svgIcon(SVGIcons.DoorOpen)
        }

        div(classes = "section-text") {
            text("Sair")
        }
    }
}