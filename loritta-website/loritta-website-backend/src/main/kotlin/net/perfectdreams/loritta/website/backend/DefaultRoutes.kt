package net.perfectdreams.loritta.website.backend

import net.perfectdreams.loritta.website.backend.routes.*

object DefaultRoutes {
    fun defaultRoutes(showtime: LorittaWebsiteBackend) = listOf(
        HomeRoute(showtime),
        SupportRoute(showtime),
        LegacyCommandsRedirectRoute(showtime),
        LegacyCommandsRoute(showtime),
        ApplicationCommandsRoute(showtime),
        ExtrasRoute(showtime),
        StaffRoute(showtime),
        ContactRoute(showtime),
        DonateRoute(showtime),
        DashboardRoute(showtime),
        BlissPlaygroundRoute(showtime),
        RandomRoute(showtime)
    )
}