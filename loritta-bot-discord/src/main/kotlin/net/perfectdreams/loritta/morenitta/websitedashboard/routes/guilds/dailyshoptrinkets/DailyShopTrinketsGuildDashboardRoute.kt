package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.dailyshoptrinkets

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.LorittaDailyShopNotificationsConfigs
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

class DailyShopTrinketsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/daily-shop-trinkets") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val databaseConfig = website.loritta.transaction {
            LorittaDailyShopNotificationsConfigs.selectAll()
                .where {
                    LorittaDailyShopNotificationsConfigs.id eq guild.idLong
                }
                .firstOrNull()
        }

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.LORITTA_TRINKETS_SHOP)
                        },
                        {
                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                        blissEvent("resyncState", "[bliss-component='save-bar']")
                                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                                    }

                                    div(classes = "hero-wrapper") {
                                        img(classes = "hero-image", src = "https://stuff.loritta.website/loritta-daily-shop-nicholas.png")

                                        div(classes = "hero-text") {
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

                                                        select {
                                                            attributes["bliss-component"] = "fancy-select-menu"
                                                            name = "shopTrinketsChannelId"
                                                            attributes["loritta-config"] = "shopTrinketsChannelId"

                                                            for (channel in guild.channels) {
                                                                if (channel is GuildMessageChannel) {
                                                                    option {
                                                                        this.label = channel.name
                                                                        this.value = channel.id
                                                                        this.disabled = false
                                                                        this.selected = (databaseConfig?.get(LorittaDailyShopNotificationsConfigs.shopTrinketsChannelId) ?: 0L) == channel.idLong
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    fieldWrapper {
                                                        fieldTitle { text(i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.ShopRefresh.MessageWhenTheShopUpdates)) }

                                                        discordMessageEditor(
                                                            guild,
                                                            MessageEditorBootstrap.TestMessageTarget.QuerySelector("[loritta-config='shopTrinketsChannelId']"),
                                                            listOf(),
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

                                                        select {
                                                            attributes["bliss-component"] = "fancy-select-menu"
                                                            name = "newTrinketsChannelId"
                                                            attributes["loritta-config"] = "newTrinketsChannelId"

                                                            for (channel in guild.channels) {
                                                                if (channel is GuildMessageChannel) {
                                                                    option {
                                                                        this.label = channel.name
                                                                        this.value = channel.id
                                                                        this.disabled = false
                                                                        this.selected = (databaseConfig?.get(LorittaDailyShopNotificationsConfigs.newTrinketsChannelId) ?: 0L) == channel.idLong
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    fieldWrapper {
                                                        fieldTitle { text(i18nContext.get(DashboardI18nKeysData.DailyShopTrinkets.NewTrinkets.MessageWhenNewTrinkets)) }

                                                        discordMessageEditor(
                                                            guild,
                                                            MessageEditorBootstrap.TestMessageTarget.QuerySelector("[loritta-config='newTrinketsChannelId']"),
                                                            listOf(),
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
        )
    }
}