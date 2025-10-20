package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection

fun FlowContent.userDashLeftSidebarEntries(
    i18nContext: I18nContext,
    selectedUserSection: UserDashboardSection
) {
    div(classes = "entry loritta-logo") {
        text("Loritta")
    }

    leftSidebarHr()

    aDashboardSidebarEntry(i18nContext, "/", "Seus Servidores", selectedUserSection == UserDashboardSection.CHOOSE_YOUR_SERVER, false)
    aDashboardSidebarEntry(i18nContext, "/user-app", i18nContext.get(DashboardI18nKeysData.PocketLoritta.Title), selectedUserSection == UserDashboardSection.POCKET_LORITTA, false)

    a(classes = "entry section-entry", href = "https://sparklypower.net/?utm_source=loritta&utm_medium=loritta-dashboard&utm_campaign=sparklylori&utm_content=user-profile-sidebar") {
        text("Servidor de Minecraft da Loritta")

        span(classes = "new-feature") {
            text("Novo!")
        }
    }

    leftSidebarHr()

    div(classes = "category") {
        text("Configurações do Usuário")
    }

    aDashboardSidebarEntry(i18nContext, "/profiles", i18nContext.get(DashboardI18nKeysData.ProfileDesigns.Title), selectedUserSection == UserDashboardSection.PROFILE_DESIGNS, false)
    aDashboardSidebarEntry(i18nContext, "/backgrounds", i18nContext.get(DashboardI18nKeysData.Backgrounds.Title), selectedUserSection == UserDashboardSection.PROFILE_BACKGROUND, false)
    aDashboardSidebarEntry(i18nContext, "/profile-presets", i18nContext.get(DashboardI18nKeysData.ProfilePresets.Title), selectedUserSection == UserDashboardSection.PROFILE_PRESETS, true)
    aDashboardSidebarEntry(i18nContext, "/ship-effects", i18nContext.get(DashboardI18nKeysData.ShipEffects.Title), selectedUserSection == UserDashboardSection.SHIP_EFFECTS, false)
    aDashboardSidebarEntry(i18nContext, "/api-keys", i18nContext.get(DashboardI18nKeysData.ApiKeys.Title), selectedUserSection == UserDashboardSection.API_KEYS, false)

    leftSidebarHr()

    div(classes = "category") {
        text("Miscelânea")
    }

    aDashboardSidebarEntry(i18nContext, "/daily-shop", i18nContext.get(DashboardI18nKeysData.DailyShop.Title), selectedUserSection == UserDashboardSection.TRINKETS_SHOP, false)
    aDashboardSidebarEntry(i18nContext, "/sonhos-shop", i18nContext.get(DashboardI18nKeysData.SonhosShop.Title), selectedUserSection == UserDashboardSection.SONHOS_SHOP, false)
    a(classes = "entry section-entry") {
        attributes["bliss-component"] = "mischievous-rascals"
        text("Pestinhas Travessas")
    }

    leftSidebarHr()
    a(classes = "entry section-entry") {
        style = "color: var(--loritta-red);"
        
        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/logout"

        text("Sair")
    }
}