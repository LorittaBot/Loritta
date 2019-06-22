package net.perfectdreams.loritta.website.routes.fanarts

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.website
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class FanArtArtistRoute : LocalizedRoute("/fanarts/{artist}") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        val test = ScriptingUtils.evaluateTemplate<Any>(
            File(
                "${LorittaWebsite.INSTANCE.config.websiteFolder}/views/fan_art_artist.kts"
            ),
            mapOf(
                "document" to "Document",
                "websiteUrl" to "String",
                "locale" to "BaseLocale",
                "artist" to "FanArtArtist",
                "user" to "User?"
            )
        )

        val queryArtistId = call.parameters["artist"]

        val fanArtist = LorittaWebsite.INSTANCE.fanArtArtists.firstOrNull { it.id == queryArtistId } ?: return
        val discordId = (fanArtist.socialNetworks?.firstOrNull { it.type == "discord" } as FanArtArtist.SocialNetwork.DiscordSocialNetwork?)?.id
        val user = discordId?.let { website.controller.discord.retrieveUserById(it) }

        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .newDocument()

        val element = test::class.members.first { it.name == "generateHtml" }.call(
            test,
            document,
            LorittaWebsite.INSTANCE.config.websiteUrl,
            locale,
            fanArtist,
            user
        ) as Element

        document.appendChild(element)

        call.respondText(LorittaWebsite.INSTANCE.transformToString(document), ContentType.Text.Html)
    }
}