package net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen

import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.i18n.I18nKeysData

class EditGuildTwitchChannelScreen(
    m: LorittaDashboardFrontend,
    guildId: Long,
    val trackedId: Long
) : GuildScreen(m, guildId) {
    override fun createPathWithArguments() = ScreenPathWithArguments(
        ScreenPath.EditGuildTwitchChannelPath,
        mapOf("guildId" to guildId.toString(), "trackedId" to trackedId.toString()),
        mapOf()
    )

    override fun createTitle() = I18nKeysData.Website.Dashboard.Twitch.Title

    override fun onLoad() {}
}