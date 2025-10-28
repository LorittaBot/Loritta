package net.perfectdreams.loritta.morenitta.websitedashboard.routes.profiles

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesigns
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignsPayments
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.Rarity
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.profileDesignItemInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trinketInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

class ProfilesUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/profiles") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        // Hacky!
        val locale = website.loritta.localeManager.getLocaleById(LocaleManager.DEFAULT_LOCALE_ID)

        val result = website.loritta.transaction {
            val profileDesigns = ProfileDesigns.selectAll().where {
                ProfileDesigns.internalName inList ProfileDesignsPayments.selectAll().where {
                    ProfileDesignsPayments.userId eq session.userId
                }.map { it[ProfileDesignsPayments.profile].value }
            }.toList()

            val settings = website.loritta.getLorittaProfile(session.userId)?.settings

            return@transaction Result(
                profileDesigns.map {
                    ProfileDesign(
                        it[ProfileDesigns.internalName],
                        it[ProfileDesigns.rarity]
                    )
                } + ProfileDesign(net.perfectdreams.loritta.morenitta.dao.ProfileDesign.DEFAULT_PROFILE_DESIGN_ID, Rarity.COMMON),
                settings?.activeProfileDesignInternalName?.value ?: net.perfectdreams.loritta.morenitta.dao.ProfileDesign.DEFAULT_PROFILE_DESIGN_ID,
                settings?.activeBackgroundInternalName?.value ?: Background.DEFAULT_BACKGROUND_ID
            )
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.ProfileDesigns.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.PROFILE_DESIGNS)
                },
                {
                    div {
                        id = "bundles-content"

                        div(classes = "bought-shop-items-list") {
                            div(classes = "loritta-items-wrapper") {
                                for (profileDesign in result.profileDesigns.sortedWith(compareByDescending<ProfileDesign> { it.rarity }.thenBy { locale["profileDesigns.${it.internalName}.title"] })) {
                                    div(classes = "shop-item-entry rarity-${profileDesign.rarity.name.lowercase()}") {
                                        attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profiles/${profileDesign.internalName}"
                                        attributes["bliss-swap:200"] = "body (innerHTML) -> #trinket-info-content (innerHTML)"
                                        attributes["bliss-indicator"] = "#trinket-info"

                                        div {
                                            style = "position: relative;"

                                            div {
                                                style = "overflow: hidden; line-height: 0;"

                                                img {
                                                    src = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-preview?type=${profileDesign.internalName}"

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
                            profileDesignItemInfo(i18nContext, locale, result.activeProfileDesignId, result.activeProfileDesignId, result.activeBackgroundId)
                        }
                    }
                }
            )
        }
    }

    private data class ProfileDesign(
        val internalName: String,
        val rarity: Rarity
    )

    private data class Result(
        val profileDesigns: List<ProfileDesign>,
        val activeProfileDesignId: String,
        val activeBackgroundId: String
    )
}