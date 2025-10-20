package net.perfectdreams.loritta.morenitta.websitedashboard.routes.apikeys

import io.ktor.server.application.*
import kotlinx.datetime.Instant
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.ShipEffect
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class APIKeysUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/api-keys") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme) {
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

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.ApiKeys.Title),
                        session,
                        theme,
                        {
                            userDashLeftSidebarEntries(i18nContext, UserDashboardSection.API_KEYS)
                        },
                        {
                            div(classes = "hero-wrapper") {
                                div(classes = "hero-text") {
                                    h1 {
                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.Title))
                                    }

                                    for (str in i18nContext.language
                                        .textBundle
                                        .lists
                                        .getValue(I18nKeys.Website.Dashboard.ApiKeys.Description.key)
                                    ) {
                                        p {
                                            handleI18nString(
                                                str,
                                                appendAsFormattedText(i18nContext, mapOf()),
                                            ) {
                                                when (it) {
                                                    "apiDocs" -> {
                                                        TextReplaceControls.ComposableFunctionResult {
                                                            // Can we not... hardcore this?
                                                            a(href = "https://loritta.website/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/developers/docs") {
                                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.ApiDocs))
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

                            div(classes = "field-wrapper") {
                                div(classes = "field-title") {
                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.YourToken))
                                }

                                div {
                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.ResetTheTokenToGetIt))
                                }

                                div(classes = "alert alert-danger") {
                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.DoNotShareYourAPIKey))
                                }

                                div {
                                    id = "user-api-key-wrapper"
                                    style = "display: flex; gap: 0.5em;"

                                    tokenInputWrapper(i18nContext, null)
                                }
                            }

                            discordButton(ButtonStyle.PRIMARY) {
                                attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/api-keys/generate"
                                attributes["bliss-swap:200"] = "body (innerHTML) -> #user-api-key-wrapper (innerHTML)"

                                style = "margin-top: 0.25em;"
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.GenerateNewToken))
                            }
                        }
                    )
                }
        )
    }
}