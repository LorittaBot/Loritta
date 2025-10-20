package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.permissions

import com.github.salomonbrys.kotson.nullBool
import com.github.salomonbrys.kotson.obj
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.numberInput
import kotlinx.html.option
import kotlinx.html.select
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands.PutCustomCommandsGuildDashboardRoute.CreateTextCommandRequest
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class PutRolePermissionsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/permissions/{roleId}") {
    @Serializable
    data class SaveRolePermissionsRequest(
        val allowInvites: Boolean,
        val ignoreCommands: Boolean,
        val bypassCommandBlacklist: Boolean
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val role = guild.getRoleById(call.parameters.getOrFail("roleId").toLong())

        if (role == null) {
            // TODO - bliss-dash: Add a proper page!
            call.respond(HttpStatusCode.NotFound)
            return
        }

        val request = Json.decodeFromString<SaveRolePermissionsRequest>(call.receiveText())

        val permissions = mutableSetOf<LorittaPermission>()
        if (request.allowInvites)
            permissions.add(LorittaPermission.ALLOW_INVITES)
        if (request.ignoreCommands)
            permissions.add(LorittaPermission.IGNORE_COMMANDS)
        if (request.bypassCommandBlacklist)
            permissions.add(LorittaPermission.BYPASS_COMMAND_BLACKLIST)

        website.loritta.transaction {
            // First we delete all of them...
            ServerRolePermissions.deleteWhere {
                ServerRolePermissions.guild eq guild.idLong and (ServerRolePermissions.roleId eq role.idLong)
            }

            for (role in guild.roles) {
                for (permission in permissions) {
                    ServerRolePermissions.insert {
                        it[ServerRolePermissions.guild] = guild.idLong
                        it[ServerRolePermissions.roleId] = role.idLong
                        it[ServerRolePermissions.permission] = permission
                    }
                }
            }
        }

        call.respondHtml(
            createHTML(false)
                .body {
                    configSaved(i18nContext)
                }
        )
    }
}