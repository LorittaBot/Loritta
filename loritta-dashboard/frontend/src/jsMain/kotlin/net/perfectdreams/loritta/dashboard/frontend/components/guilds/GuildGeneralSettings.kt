package net.perfectdreams.loritta.dashboard.frontend.components.guilds

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.components.ButtonType
import net.perfectdreams.loritta.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.dashboard.frontend.screens.ChooseYourServerScreen
import net.perfectdreams.loritta.dashboard.frontend.screens.GuildGeneralSettingsScreen
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun GuildGeneralSettings(m: LorittaDashboardFrontend, screen: GuildGeneralSettingsScreen) {
    Div {
        Text("General Settings for Guild ${screen.guildId}")

        DiscordButton(
            ButtonType.PRIMARY,
            attrs = {
                onClick {
                    m.screen = ChooseYourServerScreen()
                }
            }
        ) {
            Text("Voltar")
        }
    }
}