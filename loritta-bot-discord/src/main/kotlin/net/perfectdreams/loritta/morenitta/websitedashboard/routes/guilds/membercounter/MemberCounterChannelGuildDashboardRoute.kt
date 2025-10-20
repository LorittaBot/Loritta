package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.membercounter

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.numberInput
import kotlinx.html.option
import kotlinx.html.select
import kotlinx.html.stream.createHTML
import kotlinx.html.textArea
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.MemberCounterChannelConfigs
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.CounterThemes
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class MemberCounterChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/member-counter/{channelId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val channel = guild.getGuildMessageChannelById(call.parameters.getOrFail("channelId").toLong()) as? StandardGuildMessageChannel

        if (channel == null) {
            // TODO - bliss-dash: Add a proper page!
            call.respond(HttpStatusCode.NotFound)
            return
        }

        val channelMemberCounter = website.loritta.transaction {
            MemberCounterChannelConfigs.selectAll()
                .where {
                    MemberCounterChannelConfigs.guild eq guild.idLong and (MemberCounterChannelConfigs.channelId eq channel.idLong)
                }
                .firstOrNull()
        }

        // Hacky!
        val locale = website.loritta.localeManager.getLocaleById(LocaleManager.DEFAULT_LOCALE_ID)

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.MemberCounter.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.MEMBER_COUNTER)
                        },
                        {
                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    goBackToPreviousSectionButton(
                                        href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/member-counter",
                                    ) {
                                        text("Voltar para a lista de canais")
                                    }

                                    hr {}

                                    if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                        blissEvent("resyncState", "[bliss-component='save-bar']")
                                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                                    }

                                    sectionConfig {
                                        fieldWrappers {
                                            fieldWrapper {
                                                toggleableSection(
                                                    {
                                                        text("Ativar contador de membros")
                                                    },
                                                    {
                                                        text("Após ativar, utilize {counter} no texto do seu tópico para ativar o contador!")
                                                    },
                                                    channelMemberCounter != null,
                                                    "enabled",
                                                    true,
                                                ) {
                                                    fieldWrappers {
                                                        fieldWrapper {
                                                            fieldTitle {
                                                                text("Tópico do Canal (Será utilizado após alguém entrar/sair)")
                                                            }

                                                            textArea {
                                                                name = "topic"
                                                                attributes["loritta-config"] = "topic"
                                                                text(channelMemberCounter?.get(MemberCounterChannelConfigs.topic) ?: channel.topic ?: "")
                                                            }
                                                        }

                                                        fieldWrapper {
                                                            fieldTitle {
                                                                text("Tema do Contador de Membros")
                                                            }

                                                            select {
                                                                name = "theme"
                                                                attributes["loritta-config"] = "theme"

                                                                for (counterTheme in CounterThemes.entries) {
                                                                    option {
                                                                        label = locale[counterTheme.localizedName]
                                                                        value = counterTheme.name
                                                                        if (counterTheme == channelMemberCounter?.get(MemberCounterChannelConfigs.theme))
                                                                            selected = true
                                                                        text(counterTheme.name)
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        fieldWrapper {
                                                            fieldTitle {
                                                                text("Preenchimento com Zeros")
                                                            }

                                                            numberInput {
                                                                name = "padding"
                                                                attributes["loritta-config"] = "padding"

                                                                value = "5"
                                                                min = "1"
                                                                max = "10"
                                                                if (channelMemberCounter != null)
                                                                    value = channelMemberCounter[MemberCounterChannelConfigs.padding].toString()
                                                            }
                                                        }
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
                                    "/member-counter/${channel.idLong}"
                                )
                            }
                        }
                    )
                }
        )
    }
}