package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.dashboard.renderer.discordMessageBlock
import net.perfectdreams.loritta.dashboard.renderer.discordMessageUserGap
import net.perfectdreams.loritta.dashboard.renderer.transformedDiscordText
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons

fun FlowContent.prefixPreview(
    session: UserSession,
    prefix: String,
    lorittaUser: User
) {
    discordMessageBlock(
        session.cachedUserIdentification.globalName ?: session.cachedUserIdentification.username,
        session.getEffectiveAvatarUrl(),
        false,
        false,
        SVGIcons.CheckFat.html.toString()
    ) {
        transformedDiscordText("${prefix}ping", listOf(), listOf(), listOf())
    }

    discordMessageUserGap()

    discordMessageBlock(
        "Loritta Morenitta \uD83D\uDE18",
        lorittaUser.effectiveAvatarUrl,
        true,
        true,
        SVGIcons.CheckFat.html.toString()
    ) {
        transformedDiscordText("\uD83C\uDFD3 **|** **Pong!**", listOf(), listOf(), listOf())
    }
}