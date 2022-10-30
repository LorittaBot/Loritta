package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.entities.Role
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.util.*
import kotlin.collections.set

class ConfigurePermissionsRoute(loritta: LorittaBot) :
    RequiresGuildAuthLocalizedRoute(loritta, "/configure/permissions") {
    override suspend fun onGuildAuthenticatedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification,
        guild: Guild,
        serverConfig: ServerConfig
    ) {
        loritta as LorittaBot

        val variables = call.legacyVariables(loritta, locale)

        variables["saveType"] = "permissions"
        val roleConfig = mutableMapOf<Role, MutableMap<String, Boolean>>()
        val rolePermissions = LorittaUser.loadGuildRolesLorittaPermissions(loritta, serverConfig, guild)

        for (role in guild.roles) {
            val permissions = rolePermissions[role.idLong] ?: EnumSet.noneOf(LorittaPermission::class.java)
            val permissionMap = mutableMapOf<String, Boolean>()

            for (permission in LorittaPermission.values()) {
                permissionMap[permission.internalName] = permissions.contains(permission)
            }

            roleConfig[role] = permissionMap
        }

        variables["roleConfigs"] = roleConfig

        call.respondHtml(evaluate("permissions.html", variables))
    }
}