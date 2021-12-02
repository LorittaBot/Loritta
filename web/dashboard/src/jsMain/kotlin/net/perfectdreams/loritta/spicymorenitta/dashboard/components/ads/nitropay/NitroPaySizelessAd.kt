package net.perfectdreams.loritta.spicymorenitta.dashboard.components.ads.nitropay

import SpicyMorenitta
import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.State
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun NitroPaySizelessAd(m: SpicyMorenitta, id: String) {
    val nitroPay = m.appState.nitroPay
    when (nitroPay) {
        is State.Success -> {
            Div(attrs = {
                id("nitropay-test-ad")

                ref {
                    nitroPay.value.renderSizelessAd(
                        id
                    )

                    onDispose {}
                }
            })
        }
        is State.Loading -> Text("NitroPay is loading...")
        is State.Failure -> Text("NitroPay failed to load!")
    }
}