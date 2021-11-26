package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.AppStylesheet
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.opacity
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun GuildSidebarIcon(name: String) {
    var showTooltip by remember { mutableStateOf(false) }

    Div(
        attrs = {
            style {
                position(Position.Relative)
                fontSize(0.px)
            }
        }
    ) {
        Img(
            "https://cdn.discordapp.com/icons/268353819409252352/caf959735a24b4bba1d31bb412fef58e.png?size=4096",
        ) {
            classes(AppStylesheet.guildSidebarIcon)
            onClick {
                // active = "floppa shy"
            }

            onMouseEnter {
                console.log("Mouse Enter, yay?")
                showTooltip = true
            }

            onMouseLeave {
                showTooltip = false
            }
        }

        Div(attrs = {
            classes(AppStylesheet.guildSidebarTooltip)
            style {
                fontSize(24.px)

                if (showTooltip)
                    opacity(1)
                else
                    opacity(0)
            }
        }) {
            Text(name)
        }
    }

    console.log("Show Tooltip? $showTooltip")
}