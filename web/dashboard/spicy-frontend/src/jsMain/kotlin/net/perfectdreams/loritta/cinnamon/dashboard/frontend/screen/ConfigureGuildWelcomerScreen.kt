package net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen

import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.i18n.I18nKeysData

class ConfigureGuildWelcomerScreen(m: LorittaDashboardFrontend, guildId: Long) : GuildScreen(m, guildId) {
    override fun createPathWithArguments() = ScreenPathWithArguments(
        ScreenPath.ConfigureGuildWelcomerPath,
        mapOf(
            "guildId" to guildId.toString()
        ),
        mapOf()
    )

    override fun createTitle() = I18nKeysData.Website.Dashboard.Welcomer.Title

    override fun onLoad() {}
}