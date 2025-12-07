package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commands

import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericRemoveChannelFromListGuildDashboardRoute

class PostRemoveChannelFromListCommandChannelsConfigurationGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericRemoveChannelFromListGuildDashboardRoute(
    website,
    "/command-channels/channels/remove",
    "/command-channels/channels/remove"
)