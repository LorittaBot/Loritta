package net.perfectdreams.loritta.spicymorenitta.dashboard.components.ads.nitropay

import SpicyMorenitta
import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.State
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun NitroPayAd(m: SpicyMorenitta, id: String, adSizes: List<String>) {
    val nitroPay = m.appState.nitroPay
    when (nitroPay) {
        is State.Success -> {
            Div(attrs = {
                id(id)

                ref { htmlDivElement ->
                    nitroPay.value.renderAd(
                        htmlDivElement.id,
                        adSizes
                    )

                    onDispose {}
                }
            })
        }
        is State.Loading -> Text("NitroPay is loading...")
        is State.Failure -> Text("NitroPay failed to load!")
    }
}