package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div

@Composable
fun DiscordVerifiedBadge() {
    Div(attrs = {
        style {
            position(Position.Relative)
            width(1.25.em)
            height(1.25.em)
        }
        ref {
            onDispose {}
        }
    }) {
        Div(
            attrs = {
                ref {
                    // Flower Star
                    it.outerHTML = "<svg style=\"position: absolute; color: rgb(0, 167, 255); top: 50%; left: 50%; transform: translate(-50%,-50%);\" xmlns=\"http://www.w3.org/2000/svg\" aria-label=\"Verified\" class=\"flowerStar-1GeTsn\" aria-hidden=\"false\" width=\"16\" height=\"16\" viewBox=\"0 0 16 15.2\"><path fill=\"currentColor\" fill-rule=\"evenodd\" d=\"m16 7.6c0 .79-1.28 1.38-1.52 2.09s.44 2 0 2.59-1.84.35-2.46.8-.79 1.84-1.54 2.09-1.67-.8-2.47-.8-1.75 1-2.47.8-.92-1.64-1.54-2.09-2-.18-2.46-.8.23-1.84 0-2.59-1.54-1.3-1.54-2.09 1.28-1.38 1.52-2.09-.44-2 0-2.59 1.85-.35 2.48-.8.78-1.84 1.53-2.12 1.67.83 2.47.83 1.75-1 2.47-.8.91 1.64 1.53 2.09 2 .18 2.46.8-.23 1.84 0 2.59 1.54 1.3 1.54 2.09z\"></path></svg>"
                    onDispose {}
                }
            }
        )

        Div(
            attrs = {
                ref {
                    // Verified
                    it.outerHTML = "<svg style=\"position: absolute; top: 0; left: 0; color: white; top: 50%; left: 50%; transform: translate(-50%,-50%);\" xmlns=\"http://www.w3.org/2000/svg\" class=\"icon-1ihkOt\" aria-hidden=\"false\" width=\"16\" height=\"16\" viewBox=\"0 0 16 15.2\"><path d=\"M7.4,11.17,4,8.62,5,7.26l2,1.53L10.64,4l1.36,1Z\" fill=\"currentColor\"></path></svg>"
                    onDispose {}
                }
            }
        )
    }
}