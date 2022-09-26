package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import org.jetbrains.compose.web.dom.Code
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun InlineUserDisplay(userInfo: CachedUserInfo) {
    Div(
        attrs = {
            classes("inline-user-display")
        }
    ) {
        DiscordAvatar(userInfo.id, userInfo.discriminator, userInfo.avatarId) {
            attr("width", "24")
            attr("height", "24")
        }

        Div {
            Text(userInfo.name + "#" + userInfo.discriminator + " (")
            Code {
                Text(userInfo.id.value.toString())
            }
            Text(")")
        }
    }
}