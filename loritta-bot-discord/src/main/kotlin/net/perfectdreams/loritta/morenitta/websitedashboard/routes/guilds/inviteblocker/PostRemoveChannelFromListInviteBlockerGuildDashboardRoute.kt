package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.inviteblocker

import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericRemoveChannelFromListGuildDashboardRoute

class PostRemoveChannelFromListInviteBlockerGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericRemoveChannelFromListGuildDashboardRoute(
    website,
    "/invite-blocker/channels/remove",
    "/invite-blocker/channels/remove"
)