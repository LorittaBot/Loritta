package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpnotifications

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.LevelAnnouncementConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup.RenderType
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.channelSelectMenu
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createGuildIconUrlPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createGuildNamePlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createGuildSizePlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createMessageTemplate
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserAvatarUrlPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserDiscriminatorPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserIdPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserMentionPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserNamePlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserTagPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fancySelectMenu
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformationBlock
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configReset
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.placeholders.sections.LevelUpPlaceholders
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.levels.LevelUpAnnouncementType
import org.jetbrains.exposed.sql.selectAll
import kotlin.collections.map

class XPNotificationsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-notifications") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val announcements = website.loritta.newSuspendedTransaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)
            val levelConfig = serverConfig.levelConfig

            if (levelConfig != null) {
                LevelAnnouncementConfigs.selectAll().where {
                    LevelAnnouncementConfigs.levelConfig eq levelConfig.id
                }.toList()
            } else listOf()
        }

        val announcement = announcements.firstOrNull()

        val xpRewardsPlaceholders = LevelUpPlaceholders.placeholders.map {
            when (it) {
                LevelUpPlaceholders.GuildIconUrlPlaceholder -> createGuildIconUrlPlaceholderGroup(i18nContext, it, guild)
                LevelUpPlaceholders.GuildNamePlaceholder -> createGuildNamePlaceholderGroup(i18nContext, it, guild)
                LevelUpPlaceholders.GuildSizePlaceholder -> createGuildSizePlaceholderGroup(i18nContext, it, guild)
                LevelUpPlaceholders.UserDiscriminatorPlaceholder -> createUserDiscriminatorPlaceholderGroup(i18nContext, it, session.cachedUserIdentification.discriminator)
                LevelUpPlaceholders.UserMentionPlaceholder -> createUserMentionPlaceholderGroup(i18nContext, it, session.userId, session.cachedUserIdentification.username, session.cachedUserIdentification.globalName)
                LevelUpPlaceholders.UserNamePlaceholder -> createUserNamePlaceholderGroup(i18nContext, it, session.cachedUserIdentification.username, session.cachedUserIdentification.globalName)
                LevelUpPlaceholders.UserTagPlaceholder -> createUserTagPlaceholderGroup(i18nContext, it, session.cachedUserIdentification.username)
                LevelUpPlaceholders.LevelUpLevelPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    "1",
                    RenderType.TEXT
                )
                LevelUpPlaceholders.LevelUpNextLevelPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    "2",
                    RenderType.TEXT
                )
                LevelUpPlaceholders.LevelUpNextLevelRequiredXPPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    "1000",
                    RenderType.TEXT
                )
                LevelUpPlaceholders.LevelUpNextLevelRoleRewardPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    "",
                    RenderType.TEXT
                )
                LevelUpPlaceholders.LevelUpNextLevelTotalXPPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    "1000",
                    RenderType.TEXT
                )
                LevelUpPlaceholders.LevelUpRankingPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    "1",
                    RenderType.TEXT
                )
                LevelUpPlaceholders.LevelUpXPPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    "1000",
                    RenderType.TEXT
                )

                LevelUpPlaceholders.UserAvatarUrlPlaceholder -> createUserAvatarUrlPlaceholderGroup(i18nContext, it, session)
                LevelUpPlaceholders.UserIdPlaceholder -> createUserIdPlaceholderGroup(i18nContext, it, session.userId)
            }
        }

        val defaultLevelUpMessage = createMessageTemplate(
            i18nContext.get(DashboardI18nKeysData.XpNotifications.DefaultTemplate.Title),
            i18nContext.get(
                DashboardI18nKeysData.XpNotifications.DefaultTemplate.Content(
                    userMention = "{@user}",
                    level = "{level}",
                    xp = "{xp}"
                )
            )
        )

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.XpNotifications.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.XP_NOTIFICATIONS)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                configReset(i18nContext)
                            }

                            heroWrapper {
                                heroText {
                                    h1 {
                                        text(i18nContext.get(DashboardI18nKeysData.XpNotifications.Title))
                                    }

                                    p {
                                        text(i18nContext.get(DashboardI18nKeysData.XpNotifications.Description))
                                    }
                                }
                            }

                            hr {}

                            sectionConfig {
                                fieldWrappers {
                                    fieldWrapper {
                                        toggleableSection(
                                            {
                                                text(i18nContext.get(DashboardI18nKeysData.XpNotifications.Enable.ToggleTitle))
                                            },
                                            null,
                                            announcement != null,
                                            "enabled",
                                            true
                                        ) {
                                            fieldWrappers {
                                                fieldWrapper {
                                                    fieldInformationBlock {
                                                        fieldTitle {
                                                            text(i18nContext.get(DashboardI18nKeysData.XpNotifications.Destination.SectionTitle))
                                                        }
                                                    }

                                                    fancySelectMenu {
                                                        name = "type"
                                                        attributes["loritta-config"] = "type"

                                                        option {
                                                            value = LevelUpAnnouncementType.SAME_CHANNEL.name
                                                            selected = announcement?.get(LevelAnnouncementConfigs.type) == LevelUpAnnouncementType.SAME_CHANNEL
                                                            text(i18nContext.get(DashboardI18nKeysData.XpNotifications.Destination.Options.SameChannel))
                                                        }

                                                        option {
                                                            value = LevelUpAnnouncementType.DIRECT_MESSAGE.name
                                                            selected = announcement?.get(LevelAnnouncementConfigs.type) == LevelUpAnnouncementType.DIRECT_MESSAGE
                                                            text(i18nContext.get(DashboardI18nKeysData.XpNotifications.Destination.Options.DirectMessage))
                                                        }

                                                        option {
                                                            value = LevelUpAnnouncementType.DIFFERENT_CHANNEL.name
                                                            selected = announcement?.get(LevelAnnouncementConfigs.type) == LevelUpAnnouncementType.DIFFERENT_CHANNEL
                                                            text(i18nContext.get(DashboardI18nKeysData.XpNotifications.Destination.Options.DifferentChannel))
                                                        }
                                                    }
                                                }

                                                fieldWrapper {
                                                    fieldInformationBlock {
                                                        fieldTitle {
                                                            text(i18nContext.get(DashboardI18nKeysData.XpNotifications.Channel.SectionTitle))
                                                        }
                                                    }

                                                    channelSelectMenu(
                                                        guild,
                                                        announcement?.get(LevelAnnouncementConfigs.channelId),
                                                        {
                                                            attributes["bliss-disable-when"] = "[name='type'] != \"${LevelUpAnnouncementType.DIFFERENT_CHANNEL.name}\""
                                                            attributes["loritta-config"] = "customChannelId"
                                                            name = "customChannelId"
                                                        }
                                                    )
                                                }

                                                fieldWrapper {
                                                    toggle(
                                                        announcement?.get(LevelAnnouncementConfigs.onlyIfUserReceivedRoles) ?: false,
                                                        "onlyIfUserReceivedRoles",
                                                        true,
                                                        {
                                                            text(i18nContext.get(DashboardI18nKeysData.XpNotifications.OnlyIfReward.ToggleTitle))
                                                        },
                                                        {
                                                            text(i18nContext.get(DashboardI18nKeysData.XpNotifications.OnlyIfReward.ToggleDescription))
                                                        }
                                                    )
                                                }

                                                discordMessageEditor(
                                                    i18nContext,
                                                    guild,
                                                    { text(i18nContext.get(DashboardI18nKeysData.XpNotifications.Message.SectionLabel)) },
                                                    null,
                                                    MessageEditorBootstrap.TestMessageTarget.Unavailable,
                                                    listOf(defaultLevelUpMessage),
                                                    xpRewardsPlaceholders,
                                                    announcement?.get(LevelAnnouncementConfigs.message) ?: defaultLevelUpMessage.content,
                                                    "message"
                                                ) {
                                                    attributes["loritta-config"] = "message"
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
                                "/xp-notifications"
                            )
                        }
                    )
                }
            )
        }
    }
}