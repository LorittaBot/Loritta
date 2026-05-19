package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.honeypot

import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericAddChannelToListGuildDashboardRoute

class PostAddChannelToListHoneypotGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericAddChannelToListGuildDashboardRoute(
    website,
    "/honeypot/channels/add",
    "/honeypot/channels/remove"
)
