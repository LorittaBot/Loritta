package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Ins
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.compositionLogger
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width

@Composable
fun GoogleAdSenseAd(id: String, width: Int, height: Int) {
    Ins(attrs = {
        classes("adsbygoogle")
        style {
            display(DisplayStyle.InlineBlock)
            width(width.px)
            height(height.px)
        }
        attr("data-ad-client", "ca-pub-9989170954243288")
        attr("data-ad-slot", id)

        ref { htmlDivElement ->
            // I don't think that it is possible for us to wait until the ad is loaded
            try {
                js("(adsbygoogle = window.adsbygoogle || []).push({});")
            } catch (e: Error) {
                compositionLogger.warn(e) { "Something went wrong while trying to render Google AdSense's ad! Is the user using Adblock?" }
            }

            onDispose {
                // add clean up code here
            }
        }
    }) {}
}