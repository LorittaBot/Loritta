package net.perfectdreams.showtime.backend

import net.perfectdreams.showtime.backend.routes.ApplicationCommandsRoute
import net.perfectdreams.showtime.backend.routes.BlogPostRoute
import net.perfectdreams.showtime.backend.routes.BlogRoute
import net.perfectdreams.showtime.backend.routes.ExtrasRoute
import net.perfectdreams.showtime.backend.routes.HomeRoute
import net.perfectdreams.showtime.backend.routes.LegacyCommandsRedirectRoute
import net.perfectdreams.showtime.backend.routes.LegacyCommandsRoute
import net.perfectdreams.showtime.backend.routes.PremiumRoute
import net.perfectdreams.showtime.backend.routes.StaffRoute
import net.perfectdreams.showtime.backend.routes.SupportRoute

object DefaultRoutes {
    fun defaultRoutes(showtime: ShowtimeBackend) = listOf(
        HomeRoute(showtime),
        SupportRoute(showtime),
        LegacyCommandsRedirectRoute(showtime),
        LegacyCommandsRoute(showtime),
        ApplicationCommandsRoute(showtime),
        PremiumRoute(showtime),
        ExtrasRoute(showtime),
        BlogRoute(showtime),
        BlogPostRoute(showtime),
        StaffRoute(showtime)
    )
}