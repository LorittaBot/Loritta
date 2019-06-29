package net.perfectdreams.loritta.website.routes.guild.dashboard

import io.ktor.application.ApplicationCall
import io.ktor.response.respondText
import net.perfectdreams.loritta.api.entities.Guild
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import net.perfectdreams.loritta.website.routes.LocalizedAuthRequiredRoute
import net.perfectdreams.loritta.website.utils.identification.SimpleUserIdentification
import net.perfectdreams.loritta.website.utils.website

abstract class LocalizedGuildAuthRequiredRoute(path: String) : LocalizedAuthRequiredRoute("/guild/{guildId}$path") {
    override suspend fun onLocalizedAuthRequiredRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        discordAuth: TemmieDiscordAuth,
        userIdentification: SimpleUserIdentification
    ) {
        // TODO: Verificar se pode configurar
        val guild = website.controller.discord.retrieveGuildById(call.parameters["guildId"]!!)

        if (guild == null) {
            call.respondText("Guild does not exist... ;w;")
            return
        }

        onLocalizedGuildAuthRequiredRequest(call,
            locale,
            discordAuth,
            userIdentification,
            guild
        )
    }

    abstract suspend fun onLocalizedGuildAuthRequiredRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        discordAuth: TemmieDiscordAuth,
        userIdentification: SimpleUserIdentification,
        guild: Guild
    )
}