package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonArray
import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.legacy.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.legacy.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.select
import kotlin.collections.set

class ConfigureNashornCommandsRoute(loritta: LorittaDiscord) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/nashorn") {
    override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
        loritta as Loritta

        val nashornCommands = loritta.newSuspendedTransaction {
            CustomGuildCommands.select {
                CustomGuildCommands.guild eq serverConfig.id.value
            }.toList()
        }

        val variables = call.legacyVariables(locale)

        variables["saveType"] = "nashorn_commands"

        val feeds = JsonArray()
        nashornCommands.forEach {
            feeds.add(
                    jsonObject(
                            "jsLabel" to it[CustomGuildCommands.label],
                            "javaScript" to it[CustomGuildCommands.code]
                    )
            )
        }

        variables["commands"] = feeds.toString()

        call.respondHtml(evaluate("configure_nashorn.html", variables))
    }
}