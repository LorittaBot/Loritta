package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.DiscordUtils
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Img
import org.w3c.dom.HTMLImageElement

@Composable
fun DiscordAvatar(
    userId: UserId,
    avatarHash: String?,
    attrs: AttrBuilderContext<HTMLImageElement>
) {
    val url = DiscordUtils.getUserAvatarUrl(userId.value.toLong(), avatarHash)

    Img(url, attrs = attrs)
}