package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.honeypot

import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericRemoveChannelFromListGuildDashboardRoute

class PostRemoveChannelFromListHoneypotGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericRemoveChannelFromListGuildDashboardRoute(
    website,
    "/honeypot/channels/remove",
    "/honeypot/channels/remove"
)
