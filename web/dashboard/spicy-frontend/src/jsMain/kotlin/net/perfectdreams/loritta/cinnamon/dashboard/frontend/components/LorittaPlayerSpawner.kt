package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.entities.LorittaPlayer
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalI18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun LorittaPlayerSpawner(
    m: LorittaDashboardFrontend,
    type: LorittaPlayer.PlayerType
) {
    val i18nContext = LocalI18nContext.current

    Div(attrs = { classes("loritta-spawner") }) {
        Img(
            src = "${window.location.origin}/assets/img/${type.folderName}/repouso.png",
            attrs = {
                attr("width", "128")
            }
        )

        DiscordButton(
            DiscordButtonType.SUCCESS,
            attrs = {
                onClick {
                    m.gameState.spawnPlayer(type)
                }
            }
        ) {
            Text(i18nContext.get(I18nKeysData.Website.Dashboard.LorittaSpawner.SpawnPlayer(type.shortName)))
        }
    }
}