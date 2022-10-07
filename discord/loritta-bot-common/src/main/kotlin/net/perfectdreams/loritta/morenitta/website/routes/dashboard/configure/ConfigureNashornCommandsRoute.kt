package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonArray
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.select
import kotlin.collections.set

class ConfigureNashornCommandsRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/nashorn") {
    override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
        loritta as LorittaBot

        val nashornCommands = loritta.newSuspendedTransaction {
            CustomGuildCommands.select {
                CustomGuildCommands.guild eq serverConfig.id.value
            }.toList()
        }

        val variables = call.legacyVariables(loritta, locale)

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