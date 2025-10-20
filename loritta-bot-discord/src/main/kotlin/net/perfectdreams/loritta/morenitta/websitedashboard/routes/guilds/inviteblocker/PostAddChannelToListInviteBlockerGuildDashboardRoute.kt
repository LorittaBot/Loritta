package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.inviteblocker

import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericAddChannelToListGuildDashboardRoute

class PostAddChannelToListInviteBlockerGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericAddChannelToListGuildDashboardRoute(
    website,
    "/invite-blocker/channels/add",
    "/invite-blocker/channels/remove"
)