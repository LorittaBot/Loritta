package net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.GamerSaferVerificationUserAndRole
import net.perfectdreams.loritta.serializable.config.GuildGamerSaferConfig

class ConfigureGuildGamerSaferVerifyScreen(m: LorittaDashboardFrontend, guildId: Long) : GuildScreen(m, guildId) {
    override fun createPathWithArguments() = ScreenPathWithArguments(
        ScreenPath.ConfigureGuildGamerSaferVerifyPath,
        mapOf(
            "guildId" to guildId.toString()
        )
    )

    // TODO: Fix this
    override fun createTitle() = I18nKeysData.Website.Dashboard.SonhosShop.Title

    // TODO: Load the server configuration from the website
    //   For now we are loading on the composable itself... which isn't inherently a bad thing tbh
    override fun onLoad() {}
}