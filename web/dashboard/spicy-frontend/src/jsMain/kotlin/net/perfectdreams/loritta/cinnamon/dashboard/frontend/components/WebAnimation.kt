package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Animations
import org.jetbrains.compose.web.dom.Div

@Composable
fun WebAnimation(animation: Animations.HackyAnimation) {
    Div(
        attrs = {
            ref {
                it.innerHTML = animation.codeToBeInserted

                // woo, spooky!
                eval(animation.codeToBeEvaluated)

                onDispose {}
            }
        }
    )
}