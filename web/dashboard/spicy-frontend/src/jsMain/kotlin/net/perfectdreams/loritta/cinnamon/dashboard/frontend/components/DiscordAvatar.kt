package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.DiscordCdn
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.Image
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
            .toUrl()
    } else {
        DiscordCdn.defaultAvatar(discriminator.toInt())
            .toUrl {
                format = Image.Format.PNG // For some weird reason, the default avatars aren't available in webp format (why?)
            }
    }

    Img(url, attrs = attrs)
}