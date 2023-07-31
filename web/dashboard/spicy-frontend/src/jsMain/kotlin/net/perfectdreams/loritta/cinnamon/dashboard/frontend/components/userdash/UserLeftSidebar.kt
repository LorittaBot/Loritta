package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.GameState
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.entities.LorittaPlayer
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun UserLeftSidebar(
    m: LorittaDashboardFrontend
) {
    println("UserLeftSidebar")

    val spicyInfo = LocalSpicyInfo.current
    val i18nContext = LocalI18nContext.current

    LeftSidebar(
        m.globalState.isSidebarOpenState,
        bottom = {
            Div(attrs = { classes("user-info-wrapper") }) {
                val userIdentification = (m.globalState.userInfo as? Resource.Success<GetUserIdentificationResponse>)?.value

                if (userIdentification != null) {
                    UserInfoSidebar(userIdentification)
                }
            }
        }
    ) {
        // woo fancy!
        A(
            href = spicyInfo.legacyDashboardUrl,
            attrs = {
                classes("entry", "loritta-logo")
                attr("tabindex", "0") // Make the entry tabbable
            }) {
            Div {
                Text("Loritta")
            }
        }

        SidebarDivider()

        // SidebarEntryScreen(m, SVGIconManager.star, "Test Switch", ScreenPathWithArguments(ScreenPath.ConfigureGuildGamerSaferVerifyPath, mapOf("guildId" to "268353819409252352")))

        SidebarEntryLink(SVGIconManager.cogs, "${spicyInfo.legacyDashboardUrl}/dashboard", "Meus Servidores")

        SidebarDivider()

        SidebarCategory("Configurações do Usuário") {
            SidebarEntryLink(SVGIconManager.idCard, "${spicyInfo.legacyDashboardUrl}/user/@me/dashboard/profiles", "Layout de Perfil")
            SidebarEntryLink(SVGIconManager.images, "${spicyInfo.legacyDashboardUrl}/user/@me/dashboard/backgrounds", "Backgrounds")
            SidebarEntryScreen(m, SVGIconManager.heart, I18nKeysData.Website.Dashboard.ShipEffects.Title, ScreenPathWithArguments(ScreenPath.ShipEffectsScreenPath, emptyMap()))
        }

        SidebarDivider()

        SidebarCategory("Miscelânea") {
            SidebarEntryLink(SVGIconManager.moneyBillWave, "${spicyInfo.legacyDashboardUrl}/daily", "Daily")
            SidebarEntryLink(SVGIconManager.store, "${spicyInfo.legacyDashboardUrl}/user/@me/dashboard/daily-shop", "Loja Diária")
            SidebarEntryScreen(m, SVGIconManager.shoppingCart, I18nKeysData.Website.Dashboard.SonhosShop.Title, ScreenPathWithArguments(ScreenPath.SonhosShopScreenPath, emptyMap()))
            SidebarEntryLink(SVGIconManager.asterisk, "${spicyInfo.legacyDashboardUrl}/guidelines", "Diretrizes da Comunidade")

            Div(
                attrs = {
                    classes("entry")
                    attr("tabindex", "0") // Make the entry tabbable

                    onClick {
                        m.globalState.openCloseOnlyModal(i18nContext.get(I18nKeysData.Website.Dashboard.LorittaSpawner.PocketLoritta)) {
                            Div(attrs = {
                                style {
                                    textAlign("center")
                                }
                            }) {
                                for (text in i18nContext.get(I18nKeysData.Website.Dashboard.LorittaSpawner.DoYouWantSomeCompany)) {
                                    P {
                                        Text(text)
                                    }
                                }
                            }

                            Div(attrs = { classes("loritta-spawner-wrapper") }) {
                                Div(attrs = { classes("loritta-spawners") }) {
                                    LorittaPlayerSpawner(m, LorittaPlayer.PlayerType.LORITTA)
                                    LorittaPlayerSpawner(m, LorittaPlayer.PlayerType.PANTUFA)
                                    LorittaPlayerSpawner(m, LorittaPlayer.PlayerType.GABRIELA)
                                }

                                FieldWrappers {
                                    FieldWrapper {
                                        Div(attrs = { classes("field-title") }) {
                                            Text(i18nContext.get(I18nKeysData.Website.Dashboard.LorittaSpawner.ActivityLevel.Title))
                                        }

                                        SelectMenu(
                                            GameState.ActivityLevel
                                                .values()
                                                .map {
                                                    SelectMenuEntry(
                                                        {
                                                            Text(i18nContext.get(it.title))
                                                        },
                                                        it == m.gameState.activityLevel,
                                                        {
                                                            m.gameState.activityLevel = it
                                                        },
                                                        {}
                                                    )
                                                }
                                        )
                                    }
                                }

                                DiscordButton(
                                    DiscordButtonType.DANGER,
                                    attrs = {
                                        onClick {
                                            m.gameState.entities.forEach {
                                                it.remove()
                                            }
                                        }
                                    }
                                ) {
                                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.LorittaSpawner.CleanUp))
                                }
                            }
                        }
                    }
                }) {
                UIIcon(SVGIconManager.star) {
                    classes("icon")
                }
                Div {
                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.LorittaSpawner.PocketLoritta))
                }
            }
        }

        SidebarDivider()

        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.Center)
                }
            }
        ) {
            Ad(Ads.LEFT_SIDEBAR_AD)
        }
        // SidebarEntry("Sair")
    }
}