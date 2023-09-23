package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.welcomer

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.EtherealGambiImg
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.InlineDiscordMention
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun WelcomerWebAnimation() {
    Div(attrs = {
        classes("welcomer-web-animation")
    }) {
        EtherealGambiImg(src = "https://stuff.loritta.website/loritta-welcomer-heathecliff.png", sizes = "350px") {
            attr("style", "height: 100%; width: 100%;")
        }

        Span(attrs = {
            classes("welcome-wumpus-message")
        }) {
            Text("Welcome, ")
            InlineDiscordMention("@Wumpus")
            Text("!")

            Img(src = "https://cdn.discordapp.com/emojis/417813932380520448.png?v=1") {
                classes("discord-inline-emoji")
            }
        }
    }
}