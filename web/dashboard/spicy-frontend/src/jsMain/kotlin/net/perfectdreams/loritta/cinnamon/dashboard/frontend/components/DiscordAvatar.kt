package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.DiscordCdn
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Img
import org.w3c.dom.HTMLImageElement

@Composable
fun DiscordAvatar(
    userId: UserId,
    discriminator: String,
    avatarHash: String?,
    attrs: AttrBuilderContext<HTMLImageElement>
) {
    val url = if (avatarHash != null) {
        DiscordCdn.userAvatar(userId.value, avatarHash)
    } else {
        DiscordCdn.defaultAvatar(discriminator.toInt())
    }

    Img(url.toUrl(), attrs = attrs)
}