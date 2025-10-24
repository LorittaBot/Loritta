package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpblockers

import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericAddChannelToListGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericRemoveChannelFromListGuildDashboardRoute

class PostAddChannelXPBlockersGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericAddChannelToListGuildDashboardRoute(
    website,
    "/xp-blockers/channels/add",
    "/xp-blockers/channels/remove"
)

class PostRemoveChannelXPBlockersGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericRemoveChannelFromListGuildDashboardRoute(
    website,
    "/xp-blockers/channels/remove",
    "/xp-blockers/channels/remove"
)