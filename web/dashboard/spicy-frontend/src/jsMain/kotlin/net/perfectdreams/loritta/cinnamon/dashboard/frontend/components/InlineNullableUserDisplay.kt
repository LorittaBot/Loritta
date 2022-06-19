package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import org.jetbrains.compose.web.dom.Code
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun InlineNullableUserDisplay(id: UserId, userInfo: CachedUserInfo?) {
    if (userInfo != null) {
        InlineUserDisplay(userInfo)
    } else {
        Div(
            attrs = {
                classes("inline-user-display")
            }
        ) {
            // Because the hash is null, the avatar will be rendered as Discord's default avatar
            DiscordAvatar(id, "0000", null) {
                attr("width", "24")
                attr("height", "24")
            }

            Div {
                Text("Usuário desconhecido (")
                Code {
                    Text(id.value.toString())
                }
                Text(")")
            }
        }
    }
}