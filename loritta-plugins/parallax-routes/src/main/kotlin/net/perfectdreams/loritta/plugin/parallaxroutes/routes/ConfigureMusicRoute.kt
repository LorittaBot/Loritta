package net.perfectdreams.loritta.plugin.funky.routes

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.dashboard.configure.GenericConfigurationRoute

class ConfigureMusicRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/music", "music", "configure_music.html")