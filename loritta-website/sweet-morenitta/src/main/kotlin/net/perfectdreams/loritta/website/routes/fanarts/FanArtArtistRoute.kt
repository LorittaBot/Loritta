package net.perfectdreams.loritta.website.routes.fanarts

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.request.path
import io.ktor.response.respondText
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.website
import java.io.File
import kotlin.reflect.full.createType

class FanArtArtistRoute : LocalizedRoute("/fanarts/{artist}") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        val queryArtistId = call.parameters["artist"]

        val fanArtist = LorittaWebsite.INSTANCE.fanArtArtists.firstOrNull { it.id == queryArtistId } ?: return
        val discordId = (fanArtist.socialNetworks?.firstOrNull { it.type == "discord" } as FanArtArtist.SocialNetwork.DiscordSocialNetwork?)?.id
        val user = discordId?.let { website.controller.discord.retrieveUserById(it) }

        val html = ScriptingUtils.evaluateWebPageFromTemplate(
                File(
                        "${LorittaWebsite.INSTANCE.config.websiteFolder}/views/fan_art_artist.kts"
                ),
                mapOf(
                        "path" to call.request.path().split("/").drop(2).joinToString("/"),
                        "websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
                        "locale" to locale,
                        "artist" to fanArtist,
                        "user" to ScriptingUtils.WebsiteArgumentType(User::class.createType(nullable = true), user)
                )
        )

        call.respondText(html, ContentType.Text.Html)
    }
}