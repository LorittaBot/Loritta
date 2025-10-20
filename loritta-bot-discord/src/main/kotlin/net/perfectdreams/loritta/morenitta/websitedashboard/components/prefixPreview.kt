package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.dashboard.renderer.discordMessageBlock
import net.perfectdreams.loritta.dashboard.renderer.discordMessageUserGap
import net.perfectdreams.loritta.dashboard.renderer.transformedDiscordText
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession

fun FlowContent.prefixPreview(
    session: UserSession,
    prefix: String,
    lorittaUser: User
) {
    discordMessageBlock(
        session.globalName ?: session.username,
        session.getEffectiveAvatarUrl(),
        false,
    ) {
        transformedDiscordText("${prefix}ping", listOf(), listOf(), listOf())
    }

    discordMessageUserGap()

    discordMessageBlock(
        "Loritta Morenitta \uD83D\uDE18",
        lorittaUser.effectiveAvatarUrl,
        true,
    ) {
        transformedDiscordText("\uD83C\uDFD3 **|** **Pong!**", listOf(), listOf(), listOf())
    }
}