package net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen

import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend

sealed class GuildScreen(
    m: LorittaDashboardFrontend,
    val guildId: Long
) : Screen(m)