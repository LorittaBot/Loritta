package net.perfectdreams.loritta.spicymorenitta.dashboard.screen

sealed class Screen {
    class UserOverview(val model: UserOverviewViewModel) : Screen()
    object Test : Screen()
}