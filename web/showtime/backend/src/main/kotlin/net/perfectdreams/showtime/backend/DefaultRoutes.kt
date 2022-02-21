package net.perfectdreams.showtime.backend

import net.perfectdreams.showtime.backend.routes.CommandsRoute
import net.perfectdreams.showtime.backend.routes.ExtrasRoute
import net.perfectdreams.showtime.backend.routes.HomeRoute
import net.perfectdreams.showtime.backend.routes.PremiumRoute
import net.perfectdreams.showtime.backend.routes.SupportRoute

object DefaultRoutes {
    fun defaultRoutes(showtime: ShowtimeBackend) = listOf(
        HomeRoute(showtime),
        SupportRoute(showtime),
        CommandsRoute(showtime),
        PremiumRoute(showtime),
        ExtrasRoute(showtime)
    )
}