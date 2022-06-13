package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import org.jetbrains.compose.web.dom.Code
import org.jetbrains.compose.web.dom.Text

@Composable
fun InlineNullableUserDisplay(id: UserId, userInfo: CachedUserInfo?) {
    if (userInfo != null) {
        InlineUserDisplay(userInfo)
    } else {
        Text("Usuário desconhecido (")
        Code {
            Text(id.value.toString())
        }
        Text(")")
    }
}