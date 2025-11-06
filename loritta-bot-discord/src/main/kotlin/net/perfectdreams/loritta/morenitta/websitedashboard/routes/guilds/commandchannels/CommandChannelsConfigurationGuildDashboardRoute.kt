package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commandchannels

import io.ktor.server.application.ApplicationCall
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeysData
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
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldDescription
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformationBlock
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.saveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.placeholders.sections.BlockedCommandChannelPlaceholders
import net.perfectdreams.loritta.serializable.ColorTheme

class CommandChannelsConfigurationGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/command-channels") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val serverConfig = website.loritta.newSuspendedTransaction {
            website.loritta.getOrCreateServerConfig(guild.idLong)
        }

        val blockedCommandsPlaceholders = BlockedCommandChannelPlaceholders.placeholders.map {
            when (it) {
                BlockedCommandChannelPlaceholders.GuildIconUrlPlaceholder -> createGuildIconUrlPlaceholderGroup(
                    i18nContext,
                    it,
                    guild
                )
                BlockedCommandChannelPlaceholders.GuildNamePlaceholder -> createGuildNamePlaceholderGroup(
                    i18nContext,
                    it,
                    guild
                )
                BlockedCommandChannelPlaceholders.GuildSizePlaceholder -> createGuildSizePlaceholderGroup(
                    i18nContext,
                    it,
                    guild
                )
                BlockedCommandChannelPlaceholders.UserDiscriminatorPlaceholder -> createUserDiscriminatorPlaceholderGroup(
                    i18nContext,
                    it,
                    session.cachedUserIdentification.discriminator
                )
                BlockedCommandChannelPlaceholders.UserMentionPlaceholder -> createUserMentionPlaceholderGroup(
                    i18nContext,
                    it,
                    session.userId,
                    session.cachedUserIdentification.username,
                    session.cachedUserIdentification.globalName
                )
                BlockedCommandChannelPlaceholders.UserNamePlaceholder -> createUserNamePlaceholderGroup(
                    i18nContext,
                    it,
                    session.cachedUserIdentification.username,
                    session.cachedUserIdentification.globalName
                )
                BlockedCommandChannelPlaceholders.UserTagPlaceholder -> createUserTagPlaceholderGroup(
                    i18nContext,
                    it,
                    session.cachedUserIdentification.username
                )

                BlockedCommandChannelPlaceholders.UserAvatarUrlPlaceholder -> createUserAvatarUrlPlaceholderGroup(i18nContext, it, session)
                BlockedCommandChannelPlaceholders.UserIdPlaceholder -> createUserIdPlaceholderGroup(i18nContext, it, session.userId)
            }
        }

        val defaultDenyMessage = createMessageTemplate(
            "Padrão",
            "{@user} você não pode enviar comandos aqui!"
        )

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.CommandChannels.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.COMMAND_CHANNELS)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            div {
                                id = "section-config"

                                fieldWrappers {
                                    fieldWrapper {
                                        fieldInformationBlock {
                                            fieldTitle {
                                                text("Canais que serão proibidos usar comandos")
                                            }

                                            fieldDescription {
                                                text("Nestes canais eu irei ignorar comandos de usuários, como se eu nem estivesse lá! (Mesmo que eu esteja observando as suas mensagens para dar XP, hihi~) Caso você queira configurar que cargos específicos possam burlar a restrição, configure na seção de permissões.")
                                            }
                                        }

                                        configurableChannelListInput(
                                            i18nContext,
                                            guild,
                                            "channels",
                                            "channels",
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/command-channels/channels/add",
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/command-channels/channels/remove",
                                            serverConfig.blacklistedChannels.toSet()
                                        )
                                    }

                                    fieldWrapper {
                                        val blockedWarning = serverConfig.blacklistedWarning

                                        toggleableSection(
                                            {
                                                text("Enviar mensagem para o usuário quando ele executar comandos em canais proibidos")
                                            },
                                            {
                                                text("Caso você tenha configurado canais que sejam proibidos de usar comandos, você pode ativar esta opção para que, quando um usuário tente executar um comando em canais proibidos, eu avise que não é possível executar comandos no canal.")
                                            },
                                            serverConfig.warnIfBlacklisted,
                                            "warnIfBlacklisted",
                                            true
                                        ) {
                                            discordMessageEditor(
                                                i18nContext,
                                                guild,
                                                { text("Mensagem") },
                                                null,
                                                MessageEditorBootstrap.TestMessageTarget.Unavailable,
                                                listOf(defaultDenyMessage),
                                                blockedCommandsPlaceholders,
                                                blockedWarning ?: defaultDenyMessage.content,
                                                "blockedWarning"
                                            ) {
                                                name = "blockedWarning"
                                                attributes["loritta-config"] = "blockedWarning"
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        {
                            saveBar(
                                i18nContext,
                                false,
                                {
                                    attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/command-channels"
                                    attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
                                    attributes["bliss-headers"] = buildJsonObject {
                                        put("Loritta-Configuration-Reset", "true")
                                    }.toString()
                                }
                            ) {
                                attributes["bliss-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/command-channels"
                                attributes["bliss-include-json"] = "[loritta-config]"
                            }
                        }
                    )
                }
            )
        }
    }
}