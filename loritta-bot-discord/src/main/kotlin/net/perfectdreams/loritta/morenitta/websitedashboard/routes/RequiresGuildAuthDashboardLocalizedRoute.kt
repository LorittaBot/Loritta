package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import io.ktor.server.response.respondText
import io.ktor.server.util.getOrFail
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.serializable.ColorTheme

abstract class RequiresGuildAuthDashboardLocalizedRoute(website: LorittaDashboardWebServer, originalGuildPath: String) : RequiresUserAuthDashboardLocalizedRoute(website, "/guilds/{guildId}$originalGuildPath") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme) {
        val guildId = call.parameters.getOrFail("guildId")

        val guild = website.loritta.lorittaShards.getGuildById(guildId)

        if (guild == null) {
            call.respondText("Unknown Guild!")
            return
        }

        val member = try {
            guild.retrieveMember(UserSnowflake.fromId(session.userId)).await()
        } catch (e: ErrorResponseException) {
            onUnauthenticatedGuildRequest(call, i18nContext, session, theme)
            return
        }

        if (member.isOwner || member.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MANAGE_SERVER)) {
            onAuthenticatedGuildRequest(call, i18nContext, session, theme, guild)
        } else {
            onUnauthenticatedGuildRequest(call, i18nContext, session, theme)
        }
    }

    abstract suspend fun onAuthenticatedGuildRequest(
        call: ApplicationCall,
        i18nContext: I18nContext,
        session: UserSession,
        theme: ColorTheme,
        guild: Guild
    )

    suspend fun onUnauthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme) {
        call.respondText("Requires Guild Login!")
    }
}