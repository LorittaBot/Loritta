package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpblockers

import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericAddChannelToListGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericAddRoleToListGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericRemoveChannelFromListGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericRemoveRoleFromListGuildDashboardRoute

class PostAddRoleXPBlockersGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericAddRoleToListGuildDashboardRoute(
    website,
    "/xp-blockers/roles/add",
    "/xp-blockers/roles/remove"
)

class PostRemoveRoleXPBlockersGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericRemoveRoleFromListGuildDashboardRoute(
    website,
    "/xp-blockers/roles/remove",
    "/xp-blockers/roles/remove"
)