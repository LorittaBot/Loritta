package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.badge

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import net.perfectdreams.loritta.serializable.ColorTheme

class BadgeGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/badge") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val donationConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong).donationConfig
        }

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.Badge.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.CUSTOM_BADGE)
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
                                                text(i18nContext.get(DashboardI18nKeysData.Badge.Title))
                                            }

                                            for (str in i18nContext.language
                                                .textBundle
                                                .lists
                                                .getValue(I18nKeys.Website.Dashboard.Badge.Description.key)
                                            ) {
                                                p {
                                                    handleI18nString(
                                                        str,
                                                        appendAsFormattedText(i18nContext, emptyMap()),
                                                    ) {
                                                        when (it) {
                                                            "profileCommand" -> {
                                                                TextReplaceControls.ComposableFunctionResult(
                                                                    {
                                                                        span(classes = "discord-mention") {
                                                                            text("/perfil")
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

                                    sectionConfig {
                                        toggleableSection(
                                            {
                                                text(i18nContext.get(DashboardI18nKeysData.Badge.EnableCustomBadge))
                                            },
                                            null,
                                            donationConfig?.customBadge ?: false,
                                            "enabled",
                                            true
                                        ) {
                                            fieldWrappers {
                                                fieldWrapper {
                                                    fieldTitle {
                                                        text("Imagem da Insígnia")
                                                    }

                                                    discordButton(ButtonStyle.PRIMARY) {
                                                        openModalOnClick(
                                                            createEmbeddedModal(
                                                                "Imagem da Insígnia",
                                                                true,
                                                                {
                                                                    fileInput {
                                                                        name = "file"
                                                                    }
                                                                },
                                                                listOf(
                                                                    {
                                                                        defaultModalCloseButton(i18nContext)
                                                                    },
                                                                    {
                                                                        discordButton(ButtonStyle.PRIMARY) {
                                                                            attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/badge/upload"
                                                                            attributes["bliss-include-json"] = "[name='file']"

                                                                            text("Enviar")
                                                                        }
                                                                    }
                                                                )
                                                            )
                                                        )

                                                        text("Alterar Imagem")
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
                                        "/badge"
                                    )
                                }
                            )
                        }
                    )
                }
        )
    }
}