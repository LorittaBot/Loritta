package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordAvatar
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun UserInfoSidebar(userIdentification: GetUserIdentificationResponse) {
    Div(attrs = { classes("user-info") }) {
        DiscordAvatar(userIdentification.id, userIdentification.discriminator, userIdentification.avatarId) {
            attr("width", "24")
            attr("height", "24")
        }

        Div(attrs = { classes("user-tag") }) {
            Div(attrs = { classes("name") }) {
                Text(userIdentification.username)
            }

            Div(attrs = { classes("discriminator") }) {
                Text("#${userIdentification.discriminator}")
            }
        }
    }
}