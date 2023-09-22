package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordAvatar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun UserInfoSidebar(m: LorittaDashboardFrontend, userIdentification: GetUserIdentificationResponse) {
    Div(attrs = { classes("user-info") }) {
        DiscordAvatar(userIdentification.id, userIdentification.avatarId) {
            attr("width", "24")
            attr("height", "24")
        }

        Div(attrs = { classes("user-tag") }) {
            val globalName = userIdentification.globalName

            if (globalName != null) {
                // If the user has a global name set, use it as the name
                Div(attrs = { classes("name") }) {
                    Text(globalName)
                }

                // And then use the user's username below it
                Div(attrs = { classes("discriminator") }) {
                    Text("@${userIdentification.username}")
                }
            } else {
                Div(attrs = { classes("name") }) {
                    Text("@${userIdentification.username}")
                }
            }
        }

        Div {
            DiscordButton(
                DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
                attrs = {
                    onClick {
                        m.globalState.openThemeSelectorModal(false)
                    }
                }
            ) {
                Text("Tema")
            }
        }
    }
}