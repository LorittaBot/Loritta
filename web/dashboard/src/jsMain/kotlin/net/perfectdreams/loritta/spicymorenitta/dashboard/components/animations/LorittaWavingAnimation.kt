package net.perfectdreams.loritta.spicymorenitta.dashboard.components.animations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NoLiveLiterals
import kotlinx.browser.document
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.jsObject
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.dom.Div

// Needs to be top level!
@JsModule("./illustrations/loritta-waving.svg")
@JsNonModule
external val svgLorittaWaving: dynamic

@Composable
@NoLiveLiterals
fun LorittaWavingAnimation() {
    Div(attrs = {
        style {
            display(DisplayStyle.Contents)
            property("pointer-events", "none")
        }

        ref { htmlDivElement ->
            htmlDivElement.innerHTML = svgLorittaWaving

            val handWaveFrames = arrayOf(
                jsObject {
                    transform = "rotate(-7deg)"
                },
                jsObject {
                    transform = "rotate(7deg)"
                }
            )

            val openEyesFrames = arrayOf(
                jsObject {
                    opacity = 1
                    easing = "steps(1)"
                },
                jsObject {
                    opacity = 0 // YES, BOTH KEYFRAMES ARE NEEDED, IF ONE IS MISSING, THERE WILL BE A FRAME WHERE BOTH OF THE TRANSITIONS WON'T BE APPLIED
                    offset = 0.4
                },
                jsObject {
                    opacity = 0
                }
            )

            val closedEyesFrames = arrayOf(
                jsObject {
                    opacity = 0
                    easing = "steps(1)"
                },
                jsObject {
                    opacity = 1 // YES, BOTH KEYFRAMES ARE NEEDED, IF ONE IS MISSING, THERE WILL BE A FRAME WHERE BOTH OF THE TRANSITIONS WON'T BE APPLIED
                    offset = 0.4
                },
                jsObject {
                    opacity = 1
                }
            )

            val breatheFrames = arrayOf(
                jsObject {
                    transform = "scaleY(1.0)"
                },
                jsObject {
                    transform = "scaleY(0.99)"
                }
            )

            val handWaveTimings = jsObject {
                duration = 1000
                iterations = Double.POSITIVE_INFINITY
                direction = "alternate"
                fill = "forwards"
                delay = 0
                easing = "ease-in-out"
            }

            val breatheTimings = jsObject {
                duration = 1000
                iterations = Double.POSITIVE_INFINITY
                direction = "alternate"
                fill = "forwards"
                delay = -300
                easing = "ease-in-out"
            }

            val openEyesTimings = jsObject {
                duration = 1000
                iterations = Double.POSITIVE_INFINITY
                direction = "alternate"
                fill = "forwards"
                delay = 0
                easing = "linear"
            }

            val closedEyesTimings = jsObject {
                duration = openEyesTimings.duration
                iterations = Double.POSITIVE_INFINITY
                direction = "alternate"
                fill = "forwards"
                delay = 0
                easing = "linear"
            }

            document.querySelector(".loritta-waving .arm")
                .asDynamic()
                .animate(handWaveFrames, handWaveTimings)

            document.querySelector(".loritta-waving .open-eyes")
                .asDynamic()
                .animate(openEyesFrames, openEyesTimings)

            document.querySelector(".loritta-waving .closed-eyes")
                .asDynamic()
                .animate(closedEyesFrames, closedEyesTimings)

            document.querySelector(".loritta-waving .animation-group")
                .asDynamic()
                .animate(breatheFrames, breatheTimings)

            onDispose {}
        }
    })
}