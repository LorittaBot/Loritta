package net.perfectdreams.loritta.cinnamon.showtime.backend

import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.*

object DefaultRoutes {
    fun defaultRoutes(showtime: ShowtimeBackend) = listOf(
        HomeRoute(showtime),
        SupportRoute(showtime),
        LegacyCommandsRedirectRoute(showtime),
        LegacyCommandsRoute(showtime),
        ApplicationCommandsRoute(showtime),
        ExtrasRoute(showtime),
        StaffRoute(showtime),
        ContactRoute(showtime),
        DonateRoute(showtime)
    )
}