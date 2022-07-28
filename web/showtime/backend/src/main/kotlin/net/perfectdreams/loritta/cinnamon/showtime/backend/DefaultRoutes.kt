package net.perfectdreams.loritta.cinnamon.showtime.backend

import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.ApplicationCommandsRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.BlogPostRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.BlogRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.ContactRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.ExtrasRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.HomeRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.LegacyCommandsRedirectRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.LegacyCommandsRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.PremiumRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.StaffRoute
import net.perfectdreams.loritta.cinnamon.showtime.backend.routes.SupportRoute

object DefaultRoutes {
    fun defaultRoutes(showtime: net.perfectdreams.loritta.cinnamon.showtime.backend.ShowtimeBackend) = listOf(
        HomeRoute(showtime),
        SupportRoute(showtime),
        LegacyCommandsRedirectRoute(showtime),
        LegacyCommandsRoute(showtime),
        ApplicationCommandsRoute(showtime),
        PremiumRoute(showtime),
        ExtrasRoute(showtime),
        BlogRoute(showtime),
        BlogPostRoute(showtime),
        StaffRoute(showtime),
        ContactRoute(showtime)
    )
}