package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.autorole

import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericAddChannelToListGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericAddRoleToListGuildDashboardRoute

class PostAddRoleToListAutoroleGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericAddRoleToListGuildDashboardRoute(
    website,
    "/autorole/roles/add",
    "/autorole/roles/remove"
)