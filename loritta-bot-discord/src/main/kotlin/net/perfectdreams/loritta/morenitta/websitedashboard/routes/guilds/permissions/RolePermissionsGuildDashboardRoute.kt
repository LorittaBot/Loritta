package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.permissions

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
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
import org.jetbrains.exposed.sql.selectAll

class RolePermissionsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/permissions/{roleId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val role = guild.getRoleById(call.parameters.getOrFail("roleId").toLong())

        if (role == null) {
            // TODO - bliss-dash: Add a proper page!
            call.respond(HttpStatusCode.NotFound)
            return
        }

        val permissions = website.loritta.transaction {
            ServerRolePermissions.selectAll().where {
                ServerRolePermissions.guild eq guild.idLong and (ServerRolePermissions.roleId eq role.idLong)
            }.map { it[ServerRolePermissions.permission] }.toSet()
        }

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.Permissions.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.PERMISSIONS)
                        },
                        {
                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    goBackToPreviousSectionButton(
                                        href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/permissions",
                                    ) {
                                        text("Voltar para a lista de cargos")
                                    }

                                    hr {}

                                    if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                        blissEvent("resyncState", "[bliss-component='save-bar']")
                                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                                    }

                                    sectionConfig {
                                        fieldWrappers {
                                            fieldWrapper {
                                                toggle(
                                                    LorittaPermission.ALLOW_INVITES in permissions,
                                                    "allowInvites",
                                                    true,
                                                    {
                                                        text("Permitir enviar convites")
                                                    }
                                                )
                                            }

                                            fieldWrapper {
                                                toggle(
                                                    LorittaPermission.IGNORE_COMMANDS !in permissions,
                                                    "allowCommands",
                                                    true,
                                                    {
                                                        text("Permitir usar comandos")
                                                    },
                                                    {
                                                        text("Caso esteja ativado, eu irei processar comandos enviados por este usuário.")
                                                    }
                                                )
                                            }

                                            fieldWrapper {
                                                toggle(
                                                    LorittaPermission.BYPASS_COMMAND_BLACKLIST in permissions,
                                                    "bypassCommandBlacklist",
                                                    true,
                                                    {
                                                        text("Permitir usar comandos em qualquer canal")
                                                    },
                                                    {
                                                        text("Caso esteja ativado, eu irei permitir usar comandos em canais que foram adicionados para eu ignorar.")
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            ) {
                                genericSaveBar(
                                    i18nContext,
                                    false,
                                    guild,
                                    "/permissions/${role.idLong}"
                                )
                            }
                        }
                    )
                }
        )
    }
}