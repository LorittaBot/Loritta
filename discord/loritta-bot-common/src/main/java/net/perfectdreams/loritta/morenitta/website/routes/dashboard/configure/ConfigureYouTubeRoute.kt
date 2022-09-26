package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

class ConfigureYouTubeRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/youtube", "youtube", "configure_youtube.html")