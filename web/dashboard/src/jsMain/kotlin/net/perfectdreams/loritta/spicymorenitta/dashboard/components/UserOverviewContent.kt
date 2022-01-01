package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import SpicyMorenitta
import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.animations.LorittaWavingAnimation
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.Screen
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text

// Needs to be top level!
@JsModule("./illustrations/guilds-overview-stampbook-base-opt.svg")
@JsNonModule
external val svgGuildsOverviewStampbook: dynamic

@Composable
fun UserOverviewContent(m: SpicyMorenitta, screen: Screen.UserOverview) {
    Div(attrs = { style { width(100.percent) } }) {
        Div(attrs = { classes("section-header") }) {
            GuildsStampbookSectionHeader(screen.model.guilds)

            Div(attrs = { style {
                height(100.percent)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.RowReverse)
                justifyContent(JustifyContent.Center)
            }}) {
                LorittaWavingAnimation()
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