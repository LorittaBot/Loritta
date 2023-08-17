@file:JsExport
package net.perfectdreams.spicymorenitta.routes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.components.GetDailyRewardOverview
import net.perfectdreams.spicymorenitta.components.GotDailyRewardOverview
import org.jetbrains.compose.web.css.opacity
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

class DailyRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/daily") {
    override val keepLoadingScreen: Boolean
        get() = true

    companion object {
        private val randomEmotes = listOf(
            "/assets/img/daily/here_comes_the_money.gif",
            "/assets/img/daily/lori_rica.png",
            "/assets/img/daily/lori_woop.gif",
            "/assets/img/daily/lori_ehissoai.gif",
            "/assets/img/daily/lori_confetti.gif",
            "/assets/img/daily/lori_yay_wobbly.gif",
            "/assets/img/daily/ferret.gif"
        )
    }

    // Because we are "retro-fitting" Jetpack Compose to SpicyMorenitta, we need to act like our "renderComposable" is our own tiiiny application
    var screen by mutableStateOf<DailyScreen?>(null)
    var opacity by mutableStateOf<Double>(1.0)

    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        m.launch {
            // Start the default screen
            val getDailyRewardScreen = DailyScreen.GetDailyRewardScreen(this@DailyRoute)
            getDailyRewardScreen.onLoad()
            m.hideLoadingScreen()
            this@DailyRoute.screen = getDailyRewardScreen
        }

        renderComposable("daily-compose-wrapper") {
            H1 {
                Text("Prêmio Diário")
            }

            Div(attrs = { classes("daily-gift") }) {
                Div(attrs = { classes("scene") }) {
                    Div(attrs = { classes("cube") }) {
                        Div(attrs = { classes("cube__face", "cube__face--front") }) {
                            Img(src = "/assets/img/daily/present_side.png") {}
                        }
                        Div(attrs = { classes("cube__face", "cube__face--back") }) {
                            Img(src = "/assets/img/daily/present_side.png") {}
                        }
                        Div(attrs = { classes("cube__face", "cube__face--right") }) {
                            Img(src = "/assets/img/daily/present_side.png") {}
                        }
                        Div(attrs = { classes("cube__face", "cube__face--left") }) {
                            Img(src = "/assets/img/daily/present_side.png") {}
                        }
                        Div(attrs = { classes("cube__face", "cube__face--top") }) {
                            Img(src = "/assets/img/daily/present_top.png") {}
                        }
                        Div(attrs = { classes("cube__face", "cube__face--lace1") }) {
                            Img(src = "/assets/img/daily/present_lace.png") {}
                        }
                        Div(attrs = { classes("cube__face", "cube__face--lace2") }) {
                            Img(src = "/assets/img/daily/present_lace.png") {}
                        }
                    }
                }
            }

            Div(
                attrs = {
                    style {
                        opacity(opacity)
                    }
                }
            ) {
                when (val currentScreen = screen) {
                    is DailyScreen.GetDailyRewardScreen -> {
                        GetDailyRewardOverview(currentScreen)
                    }
                    is DailyScreen.GotDailyRewardScreen -> {
                        GotDailyRewardOverview(currentScreen)
                    }
                    // Loading or something idk lol
                    null -> {}
                }
            }
        }
    }
}