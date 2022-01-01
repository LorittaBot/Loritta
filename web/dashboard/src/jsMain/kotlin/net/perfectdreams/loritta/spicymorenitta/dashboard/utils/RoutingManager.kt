package net.perfectdreams.loritta.spicymorenitta.dashboard.utils

import SpicyMorenitta
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.Screen
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.UserOverviewViewModel

class RoutingManager(private val m: SpicyMorenitta) {
    var screenState by mutableStateOf<Screen?>(null)

    fun switchToUserOverview() {
        val viewModel = UserOverviewViewModel(m)
        viewModel.loadData()
        switch(Screen.UserOverview(viewModel))
    }

    fun switch(screen: Screen) {
        println("Switching to $screen")
        val currentScreenState = screenState
        // Automatically dispose the current screen ViewModel if the screen has a ViewModel
        if (currentScreenState is Screen.ScreenWithViewModel)
            currentScreenState.model.dispose()
        screenState = screen
        m.appState.isSidebarOpen = false // Close sidebar if it is open
    }
}