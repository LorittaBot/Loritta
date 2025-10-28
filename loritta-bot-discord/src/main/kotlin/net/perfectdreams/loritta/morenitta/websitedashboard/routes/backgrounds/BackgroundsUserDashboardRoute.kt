package net.perfectdreams.loritta.morenitta.websitedashboard.routes.backgrounds

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.Rarity
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.backgroundItemInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trinketInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

class BackgroundsUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/backgrounds") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        // Hacky!
        val locale = website.loritta.localeManager.getLocaleById(LocaleManager.DEFAULT_LOCALE_ID)

        val specialBackgrounds = listOf(
            Background(net.perfectdreams.loritta.serializable.Background.DEFAULT_BACKGROUND_ID, Rarity.UNCOMMON),
            Background(net.perfectdreams.loritta.serializable.Background.RANDOM_BACKGROUND_ID, Rarity.COMMON),
            Background(net.perfectdreams.loritta.serializable.Background.CUSTOM_BACKGROUND_ID, Rarity.COMMON),
        )

        val result = website.loritta.transaction {
            val profileDesigns = Backgrounds.selectAll().where {
                Backgrounds.internalName inList BackgroundPayments.selectAll().where {
                    BackgroundPayments.userId eq session.userId
                }.map { it[BackgroundPayments.background].value }
            }.toList()

            val settings = website.loritta.getLorittaProfile(session.userId)?.settings

            return@transaction Result(
                profileDesigns.map {
                    Background(
                        it[Backgrounds.internalName],
                        it[Backgrounds.rarity]
                    )
                } + specialBackgrounds,
                settings?.activeProfileDesignInternalName?.value ?: net.perfectdreams.loritta.morenitta.dao.ProfileDesign.DEFAULT_PROFILE_DESIGN_ID,
                settings?.activeBackgroundInternalName?.value ?: net.perfectdreams.loritta.serializable.Background.DEFAULT_BACKGROUND_ID
            )
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Backgrounds.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                null,
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.PROFILE_BACKGROUND)
                },
                {
                    div {
                        id = "bundles-content"

                        div(classes = "bought-shop-items-list") {
                            div(classes = "loritta-items-wrapper") {
                                for (profileDesign in result.backgrounds.sortedWith(compareByDescending<BackgroundsUserDashboardRoute.Background> { it.rarity }.thenBy { locale["backgrounds.${it.internalName}.title"] })) {
                                    div(classes = "shop-item-entry rarity-${profileDesign.rarity.name.lowercase()}") {
                                        attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/backgrounds/${profileDesign.internalName}"
                                        attributes["bliss-swap:200"] = "body (innerHTML) -> #trinket-info-content (innerHTML)"
                                        attributes["bliss-indicator"] = "#trinket-info"

                                        div {
                                            style = "position: relative;"

                                            div {
                                                style = "overflow: hidden; line-height: 0;"

                                                img {
                                                    src = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/background-preview/${profileDesign.internalName}"

                                                    // The aspect ratio makes the design not be wonky when the image is not loaded
                                                    style = "width: 100%; height: auto; aspect-ratio: 4/3;"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        trinketInfo {
                            backgroundItemInfo(i18nContext, locale, result.activeBackgroundId, result.activeProfileDesignId, result.activeBackgroundId)
                        }
                    }
                }
            )
        }
    }

    private data class Background(
        val internalName: String,
        val rarity: Rarity
    )

    private data class Result(
        val backgrounds: List<BackgroundsUserDashboardRoute.Background>,
        val activeProfileDesignId: String,
        val activeBackgroundId: String
    )
}