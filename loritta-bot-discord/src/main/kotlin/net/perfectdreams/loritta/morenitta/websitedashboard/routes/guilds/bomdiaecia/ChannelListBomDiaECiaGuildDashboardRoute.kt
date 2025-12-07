package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bomdiaecia

import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericAddChannelToListGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericRemoveChannelFromListGuildDashboardRoute

class PostAddChannelBomDiaECiaGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericAddChannelToListGuildDashboardRoute(
    website,
    "/bom-dia-e-cia/channels/add",
    "/bom-dia-e-cia/channels/remove"
)

class PostRemoveChannelBomDiaECiaGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericRemoveChannelFromListGuildDashboardRoute(
    website,
    "/bom-dia-e-cia/channels/remove",
    "/bom-dia-e-cia/channels/remove"
)