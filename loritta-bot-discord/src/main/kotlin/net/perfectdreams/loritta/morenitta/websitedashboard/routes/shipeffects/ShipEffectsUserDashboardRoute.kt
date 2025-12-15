package net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects

import io.ktor.server.application.ApplicationCall
import kotlinx.datetime.Instant
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.numberInput
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.textInput
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformationBlock
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.shipBuyButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.shipEffectsBribes
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.ShipEffect
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class ShipEffectsUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/ship-effects") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val activeShipEffects = website.loritta.transaction {
            ShipEffects.selectAll()
                .where {
                    ShipEffects.buyerId eq session.userId and (ShipEffects.expiresAt greater System.currentTimeMillis())
                }.map { row ->
                    ShipEffect(
                        row[ShipEffects.id].value,
                        UserId(row[ShipEffects.buyerId].toULong()),
                        UserId(row[ShipEffects.user1Id].toULong()),
                        UserId(row[ShipEffects.user2Id].toULong()),
                        row[ShipEffects.editedShipValue],
                        Instant.fromEpochMilliseconds(row[ShipEffects.expiresAt])
                    )
                }
        }

        val resolvedUsers = activeShipEffects.flatMap { listOf(it.user1, it.user2, it.buyerId) }
            .distinct()
            .mapNotNull { website.loritta.pudding.users.getCachedUserInfoById(it) }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.ShipEffects.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.SHIP_EFFECTS)
                },
                {
                    div(classes = "hero-wrapper") {
                        etherealGambiImg(
                            "https://stuff.loritta.website/ship/loritta.png",
                            classes = "hero-image",
                            sizes = "(max-width: 900px) 100vw, 360px"
                        ) {}

                        div(classes = "hero-text") {
                            h1 {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.Title))
                            }

                            for (str in i18nContext.language
                                .textBundle
                                .lists
                                .getValue(I18nKeys.Website.Dashboard.ShipEffects.Description.key)
                            ) {
                                p {
                                    handleI18nString(
                                        str,
                                        appendAsFormattedText(i18nContext, mapOf("sonhos" to 3_000)),
                                    ) {
                                        when (it) {
                                            "shipCommand" -> {
                                                TextReplaceControls.ComposableFunctionResult {
                                                    span(classes = "discord-mention") {
                                                        text("/ship")
                                                    }
                                                }
                                            }

                                            else -> TextReplaceControls.AppendControlAsIsResult
                                        }
                                    }
                                }
                            }
                        }
                    }

                    hr {}

                    h2 { text(i18nContext.get(DashboardI18nKeysData.ShipEffects.Bribe.Title)) }

                    fieldWrappers {
                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text(i18nContext.get(DashboardI18nKeysData.ShipEffects.Bribe.UserThatWillReceiveTheEffect))
                                }
                            }

                            div {
                                textInput {
                                    id = "user-input"
                                    name = "userQuery"

                                    attributes["bliss-post"] = "/br/ship-effects"
                                    attributes["bliss-trigger"] = "input"
                                    attributes["bliss-include-query"] = "#user-input"
                                    attributes["bliss-swap:200"] = ".input-result -> #message (innerHTML), #buy-button (outerHTML) -> #buy-button (outerHTML)"
                                }

                                div {
                                    id = "message"
                                }
                            }
                        }

                        fieldWrapper {
                            fieldInformationBlock {
                                fieldTitle {
                                    text(i18nContext.get(DashboardI18nKeysData.ShipEffects.Bribe.NewShipPercentage))
                                }
                            }

                            div {
                                numberInput {
                                    id = "ship-percentage"
                                    name = "shipPercentage"
                                    min = "0"
                                    max = "100"
                                    step = "1"
                                    value = "100"
                                }
                            }
                        }

                        fieldWrapper {
                            shipBuyButton(i18nContext, false)
                        }

                        hr {}

                        div {
                            id = "active-bribes"

                            shipEffectsBribes(
                                i18nContext,
                                session,
                                activeShipEffects,
                                resolvedUsers
                            )
                        }
                    }
                }
            )
        }
    }
}