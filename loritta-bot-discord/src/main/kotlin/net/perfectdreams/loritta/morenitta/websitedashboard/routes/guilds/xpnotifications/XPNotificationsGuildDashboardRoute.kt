package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpnotifications

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.LevelAnnouncementConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup.RenderType
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.channelSelectMenu
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createGuildIconUrlPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createGuildNamePlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createGuildSizePlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createMessageTemplate
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserDiscriminatorPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserMentionPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserNamePlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserTagPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fancySelectMenu
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
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.placeholders.sections.LevelUpPlaceholders
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.levels.LevelUpAnnouncementType
import org.jetbrains.exposed.sql.selectAll
import kotlin.collections.map

class XPNotificationsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-notifications") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
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
                LevelUpPlaceholders.UserDiscriminatorPlaceholder -> createUserDiscriminatorPlaceholderGroup(i18nContext, it, session.discriminator)
                LevelUpPlaceholders.UserMentionPlaceholder -> createUserMentionPlaceholderGroup(i18nContext, it, session.userId, session.username, session.globalName)
                LevelUpPlaceholders.UserNamePlaceholder -> createUserNamePlaceholderGroup(i18nContext, it, session.username, session.globalName)
                LevelUpPlaceholders.UserTagPlaceholder -> createUserTagPlaceholderGroup(i18nContext, it, session.username)
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
            }
        }

        val defaultLevelUpMessage = createMessageTemplate(
            "Padrão",
            "Parabéns {@user}, você passou para o nível **{level}** (*{xp} XP*)!"
        )

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.XpNotifications.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.XP_NOTIFICATIONS)
                        },
                        {
                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                        blissEvent("resyncState", "[bliss-component='save-bar']")
                                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                                    }

                                    heroWrapper {
                                        heroText {
                                            h1 {
                                                text(i18nContext.get(DashboardI18nKeysData.XpNotifications.Title))
                                            }

                                            p {
                                                text("Notificações ao subir de nível")
                                            }
                                        }
                                    }

                                    hr {}

                                    sectionConfig {
                                        fieldWrappers {
                                            fieldWrapper {
                                                toggleableSection(
                                                    {
                                                        text("Ativar mensagem ao subir de nível")
                                                    },
                                                    null,
                                                    announcement != null,
                                                    "enabled",
                                                    true
                                                ) {
                                                    fieldWrappers {
                                                        fieldWrapper {
                                                            fieldTitle {
                                                                text("Onde a mensagem será enviada")
                                                            }

                                                            fancySelectMenu {
                                                                name = "type"
                                                                attributes["loritta-config"] = "type"

                                                                option {
                                                                    value = LevelUpAnnouncementType.SAME_CHANNEL.name
                                                                    selected = announcement?.get(LevelAnnouncementConfigs.type) == LevelUpAnnouncementType.SAME_CHANNEL
                                                                    text("Canal Atual")
                                                                }

                                                                option {
                                                                    value = LevelUpAnnouncementType.DIRECT_MESSAGE.name
                                                                    selected = announcement?.get(LevelAnnouncementConfigs.type) == LevelUpAnnouncementType.DIRECT_MESSAGE
                                                                    text("Mensagem Direta")
                                                                }

                                                                option {
                                                                    value = LevelUpAnnouncementType.DIFFERENT_CHANNEL.name
                                                                    selected = announcement?.get(LevelAnnouncementConfigs.type) == LevelUpAnnouncementType.DIFFERENT_CHANNEL
                                                                    text("Canal Personalizado")
                                                                }
                                                            }
                                                        }

                                                        fieldWrapper {
                                                            fieldTitle {
                                                                text("Canal onde a mensagem será enviada")
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
                                                                false,
                                                                "onlyIfUserReceivedRoles",
                                                                true,
                                                                {
                                                                    text("Apenas notificar caso o usuário receba alguma recompensa")
                                                                },
                                                                {
                                                                    text("Caso você tenha configurado recompensas ao subir de nível, eu posso notificar apenas se o usuário recebeu alguma recompensa")
                                                                }
                                                            )
                                                        }

                                                        fieldWrapper {
                                                            fieldTitle {
                                                                text("Mensagem ao subir de nível")
                                                            }

                                                            discordMessageEditor(
                                                                guild,
                                                                MessageEditorBootstrap.TestMessageTarget.Unavailable,
                                                                listOf(defaultLevelUpMessage),
                                                                xpRewardsPlaceholders,
                                                                announcement?.get(LevelAnnouncementConfigs.message) ?: defaultLevelUpMessage.content
                                                            ) {
                                                                name = "message"
                                                                attributes["loritta-config"] = "message"
                                                            }
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
        )
    }
}