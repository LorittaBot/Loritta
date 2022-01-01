package net.perfectdreams.loritta.spicymorenitta.dashboard.utils

import SpicyMorenitta
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import http
import io.ktor.client.request.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.formatters.IntlMFFormatter
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.webapi.data.CreateSessionResponse
import org.w3c.dom.get

class AppState(private val m: SpicyMorenitta)  {
    var sessionToken by mutableStateOf<State<String>>(State.Loading())
    var i18nContext by mutableStateOf<State<I18nContext>>(State.Loading())
    var nitroPay by mutableStateOf<State<NitroPay>>(State.Loading())
    var isSidebarOpen by mutableStateOf(false)

    fun loadData() {
        val jobs = listOf(
            GlobalScope.async {
                val response = Json.decodeFromString<CreateSessionResponse>(http.post("http://192.168.15.14:8000/api/v1/session"))
                this@AppState.sessionToken = State.Success(response.token)
            },
            GlobalScope.async {
                val result = http.get<String>("http://192.168.15.14:8000/api/v1/languages/en") {}

                val i18nContext = I18nContext(
                    IntlMFFormatter(),
                    Json.decodeFromString(result)
                )

                println(i18nContext.get(I18nKeysData.Achievements.Achievement.IsThatAnUndertaleReference.Title))

                this@AppState.i18nContext = State.Success(i18nContext)
            }
        )

        if (window["nitroAds"] != undefined && window["nitroAds"].loaded == true) {
            nitroPay = State.Success(NitroPay(window["nitroAds"]))
        } else {
            println("NitroPay is not loaded yet! We are going to wait until the event is triggered to render the ads...")
            document.addEventListener("nitroAds.loaded", {
                // nitroAds just loaded
                nitroPay = State.Success(NitroPay(window["nitroAds"]))
            })
        }

        GlobalScope.launch {
            jobs.awaitAll()
        }
    }
}