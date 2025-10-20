package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.autorole

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableChannelList
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableRoleList
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.loadingSpinnerImage
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme

class AutoroleGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/autorole") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val autoroleConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong).autoroleConfig
        }

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.Autorole.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.AUTOROLE)
                        },
                        {
                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                        blissEvent("resyncState", "[bliss-component='save-bar']")
                                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                                    }

                                    div(classes = "hero-wrapper") {
                                        div(classes = "hero-text") {
                                            h1 {
                                                text("Autorole")
                                            }

                                            p {
                                                text("Autorole serve para você dar cargos para novos membros do seu servidor automaticamente quando eles entrarem no servidor. Chega de dar cargos para novatos manualmente!")
                                            }
                                        }
                                    }

                                    hr {}

                                    sectionConfig {
                                        toggleableSection(
                                            {
                                                text("Ativar Autorole")
                                            },
                                            null,
                                            autoroleConfig?.enabled ?: false,
                                            "enabled",
                                            true,
                                        ) {
                                            fieldWrappers {
                                                fieldWrapper {
                                                    div {
                                                        style = "display: flex; flex-gap: 0.5em;"
                                                        select {
                                                            style = "flex-grow: 1;"

                                                            name = "roleId"

                                                            for (role in guild.roles) {
                                                                if (!role.isPublicRole) {
                                                                    option {
                                                                        label = role.name
                                                                        value = role.id
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        discordButton(ButtonStyle.SUCCESS) {
                                                            id = "add-role-button"
                                                            attributes["bliss-indicator"] = "this"
                                                            attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/autorole/roles/add"
                                                            attributes["bliss-include-json"] = "[name='roleId'],[name='roles[]']"
                                                            attributes["bliss-swap:200"] = "body (innerHTML) -> #roles (innerHTML)"
                                                            attributes["bliss-sync"] = "#add-role-button"

                                                            div {
                                                                text("Adicionar")
                                                            }

                                                            div(classes = "loading-text-wrapper") {
                                                                loadingSpinnerImage()

                                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                                                            }
                                                        }
                                                    }

                                                    div {
                                                        id = "roles"

                                                        configurableRoleList(
                                                            i18nContext,
                                                            guild,
                                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/autorole/roles/remove",
                                                            autoroleConfig?.roles?.toSet() ?: setOf()
                                                        )
                                                    }
                                                }

                                                fieldWrapper {
                                                    toggle(
                                                        autoroleConfig?.giveOnlyAfterMessageWasSent ?: false,
                                                        "enabled",
                                                        true,
                                                        {
                                                            text("Dar os cargos após o usuário enviar alguma mensagem no servidor")
                                                        },
                                                        {
                                                            text("Os cargos só serão entregues após o usuário enviar uma mensagem em qualquer canal de texto do seu servidor. Recomendamos que deixe ativado, assim o usuário terá que respeitar o nível de verificação do seu servidor, já que usuários com cargos burlam o nível de verificação do Discord.")
                                                        }
                                                    )
                                                }

                                                fieldWrapper {
                                                    fieldTitle {
                                                        text("Depois de quanto tempo o cargo será dado? (Segundos)")
                                                    }

                                                    numberInput {
                                                        name = "giveRolesAfter"
                                                        attributes["loritta-config"] = "giveRolesAfter"
                                                        value = autoroleConfig?.giveRolesAfter?.toString() ?: "0"
                                                        min = "0"
                                                        max = "60"
                                                        step = "1"
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
                                    "/autorole"
                                )
                            }
                        }
                    )
                }
        )
    }
}