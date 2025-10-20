package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commandchannels

import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericAddChannelToListGuildDashboardRoute

class PostAddChannelToListCommandChannelsConfigurationGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericAddChannelToListGuildDashboardRoute(
    website,
    "/command-channels/channels/add",
    "/command-channels/channels/remove"
)