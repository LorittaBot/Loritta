package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.autorole

import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericRemoveChannelFromListGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericRemoveRoleFromListGuildDashboardRoute

class PostRemoveRoleFromListAutoroleGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericRemoveRoleFromListGuildDashboardRoute(
    website,
    "/autorole/roles/remove",
    "/autorole/roles/remove"
)