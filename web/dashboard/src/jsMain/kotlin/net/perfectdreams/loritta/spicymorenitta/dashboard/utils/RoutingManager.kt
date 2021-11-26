package net.perfectdreams.loritta.spicymorenitta.dashboard.utils

import SpicyMorenitta
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.userdash.UserOverview
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.Screen
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.UserOverviewViewModel

class RoutingManager(private val m: SpicyMorenitta) {
    var screenState = mutableStateOf<Screen?>(null)
    var delegatedScreenState by screenState
    var loading by mutableStateOf<Boolean>(false)

    fun switchToUserOverview() {
        val viewModel = UserOverviewViewModel(m)
        viewModel.loadData()
        switch(Screen.UserOverview(viewModel))
    }

    fun switch(screen: Screen) {
        println("Switching to $screen")
        delegatedScreenState = screen
        loading = false
    }
}