package net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen

import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.i18n.I18nKeysData

class AddNewGuildTwitchChannelScreen(
    m: LorittaDashboardFrontend,
    guildId: Long,
    val userId: Long,
    val createPremiumTrack: Boolean
) : GuildScreen(m, guildId) {
    override fun createPathWithArguments() = ScreenPathWithArguments(
        ScreenPath.AddNewGuildTwitchChannelPath,
        mapOf("guildId" to guildId.toString()),
        mapOf("userId" to userId.toString(), "createPremiumTrack" to createPremiumTrack.toString())
    )

    override fun createTitle() = I18nKeysData.Website.Dashboard.Twitch.Title

    override fun onLoad() {}
}