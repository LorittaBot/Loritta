package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.dailyshoptrinkets

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.LorittaDailyShopNotificationsConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.channelSelectMenu
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.simpleHeroImage
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.placeholders.sections.DailyShopTrinketsPlaceholders
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class DailyShopTrinketsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/daily-shop-trinkets") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val databaseConfig = website.loritta.transaction {
            LorittaDailyShopNotificationsConfigs.selectAll()
                .where {
                    LorittaDailyShopNotificationsConfigs.id eq guild.idLong
                }
                .firstOrNull()
        }

        val placeholderGroups = DailyShopTrinketsPlaceholders.placeholders.map {
            when (it) {
                DailyShopTrinketsPlaceholders.DailyShopDateShortPlaceholder -> createPlaceholderGroup(
                    it.placeholders,
                    null,
                    TimeFormat.DATE_SHORT.format(Instant.now()),
                    MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                )
                DailyShopTrinketsPlaceholders.GuildIconUrlPlaceholder -> createPlaceholderGroup(
                    it.placeholders,
                    null,
                    guild.iconUrl ?: "???",
                    MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                )
                DailyShopTrinketsPlaceholders.GuildNamePlaceholder -> createPlaceholderGroup(
                    it.placeholders,
                    null,
                    guild.name,
                    MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                )
                DailyShopTrinketsPlaceholders.GuildSizePlaceholder -> createPlaceholderGroup(
                    it.placeholders,
                    null,
                    guild.memberCount.toString(),
                    MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                )
            }
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.LORITTA_TRINKETS_SHOP)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                blissEvent("resyncState", "[bliss-component='save-bar']")
                                blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                            }

                            heroWrapper {
                                simpleHeroImage("https://stuff.loritta.website/loritta-daily-shop-nicholas.png")

                                heroText {
                                    h1 {
                                        text(i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.Title))
                                    }

                                    for (str in i18nContext.language
                                        .textBundle
                                        .lists
                                        .getValue(I18nKeys.Website.Dashboard.DailyShopTrinkets.Description.key)
                                    ) {
                                        p {
                                            handleI18nString(
                                                str,
                                                appendAsFormattedText(i18nContext, emptyMap()),
                                            ) {
                                                when (it) {
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
                                    toggleableSection(
                                        {
                                            text(i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.ShopRefresh.NotifyWhenRefresh))
                                        },
                                        {
                                            text(i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.ShopRefresh.NotifyWhenRefreshDescription))
                                        },
                                        databaseConfig?.get(LorittaDailyShopNotificationsConfigs.notifyShopTrinkets) ?: false,
                                        "notifyShopTrinkets",
                                        true
                                    ) {
                                        fieldWrappers {
                                            fieldWrapper {
                                                fieldTitle { text(i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.ShopRefresh.ChannelWhereTheMessagesWillBeSent)) }

                                                channelSelectMenu(
                                                    guild,
                                                    databaseConfig?.get(LorittaDailyShopNotificationsConfigs.shopTrinketsChannelId),
                                                ) {
                                                    attributes["loritta-config"] = "shopTrinketsChannelId"
                                                    name = "shopTrinketsChannelId"
                                                }
                                            }

                                            fieldWrapper {
                                                fieldTitle { text(i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.ShopRefresh.MessageWhenTheShopUpdates)) }

                                                discordMessageEditor(
                                                    guild,
                                                    MessageEditorBootstrap.TestMessageTarget.QuerySelector("[loritta-config='shopTrinketsChannelId']"),
                                                    listOf(),
                                                    placeholderGroups,
                                                    databaseConfig?.get(LorittaDailyShopNotificationsConfigs.shopTrinketsMessage) ?: ""
                                                ) {
                                                    attributes["loritta-config"] = "shopTrinketsMessage"
                                                    name = "shopTrinketsMessage"
                                                }
                                            }
                                        }
                                    }

                                    toggleableSection(
                                        {
                                            text(i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.NewTrinkets.NotifyWhenNew))
                                        },
                                        {
                                            text(i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.NewTrinkets.NotifyWhenNewDescription))
                                        },
                                        databaseConfig?.get(LorittaDailyShopNotificationsConfigs.notifyNewTrinkets) ?: false,
                                        "notifyNewTrinkets",
                                        true
                                    ) {
                                        fieldWrappers {
                                            fieldWrapper {
                                                fieldTitle { text(i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.NewTrinkets.ChannelWhereTheMessagesWillBeSent)) }

                                                channelSelectMenu(
                                                    guild,
                                                    databaseConfig?.get(LorittaDailyShopNotificationsConfigs.shopTrinketsChannelId),
                                                ) {
                                                    attributes["loritta-config"] = "newTrinketsChannelId"
                                                    name = "newTrinketsChannelId"
                                                }
                                            }

                                            fieldWrapper {
                                                fieldTitle { text(i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.NewTrinkets.MessageWhenNewTrinkets)) }

                                                discordMessageEditor(
                                                    guild,
                                                    MessageEditorBootstrap.TestMessageTarget.QuerySelector("[loritta-config='newTrinketsChannelId']"),
                                                    listOf(),
                                                    placeholderGroups,
                                                    databaseConfig?.get(LorittaDailyShopNotificationsConfigs.newTrinketsMessage) ?: ""
                                                ) {
                                                    attributes["loritta-config"] = "newTrinketsMessage"
                                                    name = "newTrinketsMessage"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        {
                            genericSaveBar(
                                i18nContext,
                                false,
                                guild,
                                "/daily-shop-trinkets"
                            )
                        }
                    )
                }
            )
        }
    }
}