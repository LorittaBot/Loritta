package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.inviteblocker

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableChannelListInput
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createGuildIconUrlPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createGuildNamePlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createGuildSizePlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createMessageTemplate
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserAvatarUrlPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserDiscriminatorPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserIdPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserMentionPlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserNamePlaceholderGroup
import net.perfectdreams.loritta.morenitta.websitedashboard.components.createUserTagPlaceholderGroup
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
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.placeholders.sections.BlockedCommandChannelPlaceholders
import net.perfectdreams.loritta.placeholders.sections.InviteBlockedPlaceholders
import net.perfectdreams.loritta.serializable.ColorTheme
import kotlin.collections.map

class InviteBlockerGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/invite-blocker") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val inviteBlockerConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong).inviteBlockerConfig
        }

        val inviteBlockedPlaceholders = InviteBlockedPlaceholders.placeholders.map {
            when (it) {
                InviteBlockedPlaceholders.GuildIconUrlPlaceholder -> createGuildIconUrlPlaceholderGroup(i18nContext, it, guild)
                InviteBlockedPlaceholders.GuildNamePlaceholder -> createGuildNamePlaceholderGroup(i18nContext, it, guild)
                InviteBlockedPlaceholders.GuildSizePlaceholder -> createGuildSizePlaceholderGroup(i18nContext, it, guild)
                InviteBlockedPlaceholders.UserDiscriminatorPlaceholder -> createUserDiscriminatorPlaceholderGroup(i18nContext, it, session.discriminator)
                InviteBlockedPlaceholders.UserMentionPlaceholder -> createUserMentionPlaceholderGroup(i18nContext, it, session.userId, session.username, session.globalName)
                InviteBlockedPlaceholders.UserNamePlaceholder -> createUserNamePlaceholderGroup(i18nContext, it, session.username, session.globalName)
                InviteBlockedPlaceholders.UserTagPlaceholder -> createUserTagPlaceholderGroup(i18nContext, it, session.username)
                InviteBlockedPlaceholders.UserAvatarUrlPlaceholder -> createUserAvatarUrlPlaceholderGroup(i18nContext, it, session)
                InviteBlockedPlaceholders.UserIdPlaceholder -> createUserIdPlaceholderGroup(i18nContext, it, session.userId)
            }
        }

        val defaultDenyMessage = createMessageTemplate(
            "Padrão",
            "{@user} você não pode enviar convites aqui!"
        )

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.InviteBlocker.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.INVITE_BLOCKER)
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
                                                text("Bloqueador de Convites")
                                            }

                                            p {
                                                text("Bloqueador de Convites")
                                            }
                                        }
                                    }

                                    hr {}

                                    div {
                                        id = "section-config"

                                        toggleableSection(
                                            {
                                                text("Ativar Bloqueador de Convites")
                                            },
                                            null,
                                            inviteBlockerConfig?.enabled ?: false,
                                            "enabled",
                                            true
                                        ) {
                                            fieldWrappers {
                                                fieldWrapper {
                                                    toggle(
                                                        inviteBlockerConfig?.whitelistServerInvites ?: false,
                                                        "allowServerInvites",
                                                        true,
                                                        {
                                                            text("Permitir compartilhar convites do servidor atual")
                                                        }
                                                    )
                                                }

                                                fieldWrapper {
                                                    toggle(
                                                        inviteBlockerConfig?.deleteMessage ?: false,
                                                        "deleteMessageOnInvite",
                                                        true,
                                                        {
                                                            text("Deletar a mensagem do usuário quando um invite for detectado")
                                                        }
                                                    )
                                                }

                                                fieldWrapper {
                                                    fieldTitle {
                                                        text("Canais aonde são permitidos enviar convites")
                                                    }

                                                    configurableChannelListInput(
                                                        i18nContext,
                                                        guild,
                                                        "channels",
                                                        "channels",
                                                        "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/invite-blocker/channels/add",
                                                        "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/invite-blocker/channels/remove",
                                                        inviteBlockerConfig?.whitelistedChannels?.toSet() ?: setOf()
                                                    )
                                                }

                                                toggleableSection(
                                                    {
                                                        text("Enviar uma mensagem ao usuário quando ele enviar um invite")
                                                    },
                                                    null,
                                                    inviteBlockerConfig?.tellUser ?: false,
                                                    "sendMessageOnInvite",
                                                    true,
                                                ) {
                                                    discordMessageEditor(
                                                        guild,
                                                        MessageEditorBootstrap.TestMessageTarget.Unavailable,
                                                        listOf(defaultDenyMessage),
                                                        inviteBlockedPlaceholders,
                                                        inviteBlockerConfig?.warnMessage ?: defaultDenyMessage.content
                                                    ) {
                                                        name = "message"
                                                        attributes["loritta-config"] = "message"
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            ) {
                                genericSaveBar(
                                    i18nContext,
                                    false,
                                    guild,
                                    "/invite-blocker"
                                )
                            }
                        }
                    )
                }
        )
    }
}