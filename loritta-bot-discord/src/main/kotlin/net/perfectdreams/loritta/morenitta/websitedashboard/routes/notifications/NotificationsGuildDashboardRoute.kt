package net.perfectdreams.loritta.morenitta.websitedashboard.routes.notifications

import io.ktor.server.application.*
import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserNotificationSettings
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.DirectMessageNotification
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.selectAll

class NotificationsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/notifications") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val notifications = website.loritta.transaction {
            UserNotificationSettings.selectAll()
                .where {
                    UserNotificationSettings.userId eq session.userId
                }
                .associate { it[UserNotificationSettings.type] to it[UserNotificationSettings.enabled] }
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Notifications.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.NOTIFICATIONS)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                blissEvent("resyncState", "[bliss-component='save-bar']")
                                blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                            }

                            div(classes = "hero-wrapper") {
                                div(classes = "hero-text") {
                                    h1 {
                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.Notifications.Title))
                                    }

                                    for (str in i18nContext.language
                                        .textBundle
                                        .lists
                                        .getValue(I18nKeys.Website.Dashboard.Notifications.Description.key)
                                    ) {
                                        p {
                                            handleI18nString(
                                                str,
                                                appendAsFormattedText(i18nContext, mapOf()),
                                            ) {
                                                when (it) {
                                                    "notificationsConfigureCommand" -> {
                                                        TextReplaceControls.ComposableFunctionResult(
                                                            {
                                                                span("discord-mention") {
                                                                    text("/")
                                                                    text(i18nContext.get(I18nKeysData.Commands.Command.Notifications.Label))
                                                                    text(" ")
                                                                    text(i18nContext.get(I18nKeysData.Commands.Command.Notifications.Configure.Label))
                                                                }
                                                            }
                                                        )
                                                    }
                                                    else -> TextReplaceControls.AppendControlAsIsResult
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            hr {}

                            div {
                                id = "section-config"

                                fieldWrappers {
                                    for (notification in DirectMessageNotification.all) {
                                        fieldWrapper {
                                            toggle(
                                                notifications[notification.type] ?: true,
                                                notification.type.name,
                                                true,
                                                { text(i18nContext.get(notification.title)) },
                                                { text(i18nContext.get(notification.description)) }
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        {
                            genericUserSaveBar(
                                i18nContext,
                                false,
                                "/notifications"
                            )
                        }
                    )
                }
            )
        }
    }
}