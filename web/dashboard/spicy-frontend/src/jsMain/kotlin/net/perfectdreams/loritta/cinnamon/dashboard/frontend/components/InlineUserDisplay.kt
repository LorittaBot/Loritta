package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.serializable.CachedUserInfo
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
        DiscordAvatar(userInfo.id, userInfo.avatarId) {
            attr("width", "24")
            attr("height", "24")
        }

        Div {
            val globalName = userInfo.globalName

            if (globalName != null) {
                Text("$globalName (")
                Text("@${userInfo.name} / ")
                Code {
                    Text(userInfo.id.value.toString())
                }
                Text(")")
            } else {
                Text(userInfo.name + " (")
                Code {
                    Text(userInfo.id.value.toString())
                }
                Text(")")
            }
        }
    }
}