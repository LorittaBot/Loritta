package net.perfectdreams.loritta.spicymorenitta.dashboard.styles

import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.StyleSheet
import org.jetbrains.compose.web.css.deg
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.transform

object LoriArmWaveAnimationStylesheet : StyleSheet(AppStylesheet) {
    @OptIn(ExperimentalComposeWebApi::class)
    val armWaveKeyframes by keyframes {
        this.each(0.percent) {
            transform {
                rotate((-5).deg)
            }
        }

        this.each(50.percent) {
            transform {
                rotate((5).deg)
            }
        }

        this.each(100.percent) {
            transform {
                rotate((-5).deg)
            }
        }
    }
}