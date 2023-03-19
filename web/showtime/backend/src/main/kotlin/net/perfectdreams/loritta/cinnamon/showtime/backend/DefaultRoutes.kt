package net.perfectdreams.loritta.cinnamon.showtime.backend

import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.ApplicationCommandsRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.ContactRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.ExtrasRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.HomeRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.LegacyCommandsRedirectRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.LegacyCommandsRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.StaffRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.SupportRoute

object DefaultRoutes {
    fun defaultRoutes(showtime: ShowtimeBackend) = listOf(
        HomeRoute(showtime),
        SupportRoute(showtime),
        LegacyCommandsRedirectRoute(showtime),
        LegacyCommandsRoute(showtime),
        ApplicationCommandsRoute(showtime),
        ExtrasRoute(showtime),
        StaffRoute(showtime),
        ContactRoute(showtime)
    )
}