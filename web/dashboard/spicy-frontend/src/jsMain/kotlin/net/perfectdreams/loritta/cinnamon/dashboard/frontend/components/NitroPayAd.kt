package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.NitroPayUtils
import org.jetbrains.compose.web.dom.Div

@Composable
fun NitroPayAd(id: String, width: Int, height: Int) {
    when (NitroPayUtils.nitroPayLoaded) {
        true -> {
            Div(attrs = {
                id(id)

                ref { htmlDivElement ->
                    NitroPayUtils.createAd(htmlDivElement, width, height)

                    onDispose {
                        // add clean up code here
                    }
                }
            }) {}
        }
        false -> {}
    }
}