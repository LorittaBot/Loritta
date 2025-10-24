package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.RolesByExperience
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableRoleRewards
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fancyRadioInput
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.growInputWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.controlsWithButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.inlinedControls
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.roleSelectMenu
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.levels.RoleGiveType
import org.jetbrains.exposed.sql.selectAll

class XPRewardsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-rewards") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val (levelConfig, rolesByExperience) = website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            val rolesByExperience = RolesByExperience.selectAll().where {
                RolesByExperience.guildId eq guild.idLong
            }.toList()

            Pair(serverConfig.levelConfig, rolesByExperience)
        }

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.XpRewards.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.XP_REWARDS)
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
                                                text(i18nContext.get(DashboardI18nKeysData.XpRewards.Title))
                                            }

                                            p {
                                                text("Recompense usuários ativos do seu servidor com cargos únicos e exclusivos.")
                                            }
                                        }
                                    }

                                    hr {}

                                    sectionConfig {
                                        fieldWrappers {
                                            fieldWrapper {
                                                fieldTitle {
                                                    text("Estilo de Recompensas por Cargo")
                                                }

                                                fancyRadioInput({
                                                    name = "roleGiveType"
                                                    attributes["loritta-config"] = "roleGiveType"
                                                    value = RoleGiveType.STACK.name

                                                    checked = levelConfig?.roleGiveType == RoleGiveType.STACK || levelConfig?.roleGiveType == null
                                                }) {
                                                    div(classes = "radio-option-info") {
                                                        div(classes = "radio-option-title") {
                                                            text("Empilhar recompensas anteriores")
                                                        }

                                                        div(classes = "radio-option-description") {
                                                            text("Ao executar um comando, eu irei deletar a mensagem do usuário.")
                                                        }
                                                    }
                                                }

                                                fancyRadioInput({
                                                    name = "roleGiveType"
                                                    attributes["loritta-config"] = "roleGiveType"
                                                    value = RoleGiveType.REMOVE.name
                                                    checked = levelConfig?.roleGiveType == RoleGiveType.REMOVE
                                                }) {
                                                    div(classes = "radio-option-info") {
                                                        div(classes = "radio-option-title") {
                                                            text("Remover recompensas anteriores")
                                                        }

                                                        div(classes = "radio-option-description") {
                                                            text("Ao subir de nível, todas as recompensas que o usuário recebeu são removidas")
                                                        }
                                                    }
                                                }
                                            }

                                            fieldWrapper {
                                                div {
                                                    fieldTitle {
                                                        text("Recompensas ao Subir de Nível")
                                                    }

                                                    controlsWithButton {
                                                        inlinedControls {
                                                            span {
                                                                text("Ao chegar em ")
                                                            }

                                                            numberInput {
                                                                name = "xp"
                                                                placeholder = "1000"
                                                                style = "width: 100px;"
                                                                value = "1000"
                                                                min = "0"
                                                                step = "1000"

                                                                attributes["bliss-post"] = "/${i18nContext.get(I18nKeys.Website.LocalePathId)}/guilds/${guild.idLong}/xp-rewards/xp2level"
                                                                attributes["bliss-swap:200"] = "body (innerHTML) -> #calculated-level (innerHTML)"
                                                                attributes["bliss-include-json"] = "[name='xp']"
                                                                attributes["bliss-trigger"] = "input"
                                                                attributes["xp-action-add-element"] = "true"
                                                            }

                                                            span {
                                                                text(" XP ")

                                                                text("(")
                                                                span {
                                                                    id = "calculated-level"
                                                                    text("Nível 1")
                                                                }
                                                                text(")")
                                                                text(", dar o cargo ")
                                                            }

                                                            growInputWrapper {
                                                                roleSelectMenu(guild, null) {
                                                                    name = "roleId"
                                                                    attributes["xp-action-add-element"] = "true"
                                                                }
                                                            }
                                                        }

                                                        discordButton(ButtonStyle.SUCCESS) {
                                                            attributes["bliss-post"] = "/${i18nContext.get(I18nKeys.Website.LocalePathId)}/guilds/${guild.idLong}/xp-rewards/add"
                                                            attributes["bliss-include-json"] = "[xp-action-add-element]"
                                                            attributes["bliss-swap:200"] = "body (innerHTML) -> #role-rewards (innerHTML)"
                                                            text("Adicionar")
                                                        }
                                                    }
                                                }

                                                div {
                                                    id = "role-rewards"

                                                    configurableRoleRewards(
                                                        i18nContext,
                                                        guild,
                                                        rolesByExperience.flatMap {
                                                            it[RolesByExperience.roles].map { roleId ->
                                                                RoleReward(
                                                                    roleId,
                                                                    it[RolesByExperience.requiredExperience]
                                                                )
                                                            }
                                                        }
                                                    )
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
                                        "/xp-rewards"
                                    )
                                }
                            )
                        }
                    )
                }
        )
    }
}