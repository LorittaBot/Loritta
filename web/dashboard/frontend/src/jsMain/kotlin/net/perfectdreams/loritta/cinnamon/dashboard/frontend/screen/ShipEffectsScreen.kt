package net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetShipEffectsResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State

class ShipEffectsScreen(val m: LorittaDashboardFrontend) : Screen() {
    var activeShipEffects by mutableStateOf<State<GetShipEffectsResponse>>(State.Loading())

    override fun onLoad() {
        launch {
            updateActiveShipEffects()
        }
    }

    private suspend fun updateActiveShipEffects() {
        delay(2_000)

        // TODO: It would be cool if we moved this code to a method that automatically changes the state for us!
        activeShipEffects = State.Loading()

        val result = m.http.get("${window.location.origin}/api/v1/users/ship-effects").bodyAsText()

        activeShipEffects = State.Success(Json.decodeFromString(result))
    }
}