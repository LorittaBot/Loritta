package net.perfectdreams.loritta.sweetmorenitta.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.HEAD
import kotlinx.html.meta
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.utils.config.FanArtArtist

class FanArtArtistView(
    locale: BaseLocale,
    path: String,
    val artist: FanArtArtist,
    val user: JDAUser?
) : NavbarView(
        locale,
        path
) {
    override fun getTitle() = "Fan Arts"

    override fun HEAD.generateMeta() {
        meta("theme-color", "#00c1df")
        meta(content = "Fan Arts da Loritta") { attributes["property"] = "og:site_name" }
        meta(content = "Veja as incríveis ${artist.fanArts.size} fan arts que ${user?.name ?: artist.info.name} fez com muito amor e carinho para mim! ✨") { attributes["property"] = "og:description" }
        meta(content = "Fan Arts de ${user?.name ?: artist.info.name}") { attributes["property"] = "og:title" }
        meta(content = "600") { attributes["property"] = "og:ttl" }
        meta(content = user?.avatarUrl ?: "https://cdn.discordapp.com/emojis/523176710439567392.png?v=1") { attributes["property"] = "og:image"}
    }

    override fun DIV.generateContent() {
        // Generated in the frontend
    }
}