package net.perfectdreams.loritta.spicymorenitta.dashboard.components.animations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NoLiveLiterals
import kotlinx.browser.document
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.jsObject
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.dom.Div

@Composable
@NoLiveLiterals
fun LorittaWavingAnimation() {
    Div(attrs = {
        style {
            display(DisplayStyle.Contents)
            property("pointer-events", "none")
        }

        ref { htmlDivElement ->
            htmlDivElement.innerHTML = """
<svg style="height: 100%; width: auto;" class="loritta-waving" xmlns="http://www.w3.org/2000/svg" height="1784" preserveAspectRatio="xMidYMid meet" width="1441" viewBox="0 0 1441 1784">
  <g class="animation-group" style="transform-origin: bottom center;">
    <foreignObject class="base" y="0" width="1602" height="3000" preserveAspectRatio="none" x="367">
        <img xmlns="http://www.w3.org/1999/xhtml" src="lori_wave_v3_base.png">
    </foreignObject>
    <foreignObject class="open-eyes" width="415" height="169" preserveAspectRatio="none" y="300" x="645">
        <img xmlns="http://www.w3.org/1999/xhtml" style="width: 100%; height: 100%;" src="lori_wave_v3_eyes.png">
    </foreignObject>
    <foreignObject class="closed-eyes" width="377" height="74" preserveAspectRatio="none" y="367" x="672">
        <img xmlns="http://www.w3.org/1999/xhtml" style="width: 100%; height: 100%;" src="lori_wave_v3_closed_eyes.png">
    </foreignObject>
    <foreignObject class="arm" width="428" height="1172" preserveAspectRatio="none" style="transform-origin: 79% 89%; transform-box: fill-box;" y="602" x="135">
        <img xmlns="http://www.w3.org/1999/xhtml" style="width: 100%; height: 100%;" src="lori_wave_arm.png">
    </foreignObject>
  </g>
</svg>
            """.trimIndent()

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