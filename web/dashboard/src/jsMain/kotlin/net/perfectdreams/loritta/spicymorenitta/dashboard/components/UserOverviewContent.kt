package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.animations.LorittaWavingAnimation
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.Screen
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.AppStylesheet
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text

@Composable
fun UserOverviewContent(screen: Screen.UserOverview) {
    Div(attrs = { style { width(100.percent) } }) {
        Div(attrs = { classes(AppStylesheet.sectionHeaderImage) }) {
            Div(attrs = { style {
                height(100.percent)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.RowReverse)
            }}) {
                LorittaWavingAnimation()
                // LoriHandWaveX()
            }
        }

        H1 {
            Text("Olá MrPowerGamerBR!")
        }

        /* H2 {
            Text("Servidores Favoritos")
        }
        Hr {}

        H2 {
            Text("Servidores Editados Recentemente")
        }
        Hr {} */

        Div {
            GuildOverviewCards(screen)
        }

        Hr {}
    }
}