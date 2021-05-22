package net.perfectdreams.loritta.plugin.donatorsostentation

import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.donatorsostentation.commands.ActivePremiumSlotsExecutor
import net.perfectdreams.loritta.plugin.donatorsostentation.commands.DisableBoostExecutor
import net.perfectdreams.loritta.plugin.donatorsostentation.commands.EnableBoostExecutor
import net.perfectdreams.loritta.plugin.donatorsostentation.modules.CheckBoostStatusModule
import net.perfectdreams.loritta.utils.HoconUtils.decodeFromFile
import java.io.File

class DonatorsOstentationPlugin(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onEnable() {
        super.onEnable()

        val config = Constants.HOCON.decodeFromFile<DonatorsOstentationConfig>(File(dataFolder, "config.conf"))

        // addEventListener(BoostGuildListener(config))
        launch(NitroBoostUtils.createBoostTask(config))
        launch(NitroBoostUtils.updateValidBoostServers(config))
        launch(PremiumSlotsUtils.createPremiumSlotsAdvertisementTask(config))
        launch(PremiumSlotsUtils.createPremiumSlotsCloseTask(config))
        addMessageReceivedModule(CheckBoostStatusModule(config))

        loriToolsExecutors.add(EnableBoostExecutor)
        loriToolsExecutors.add(DisableBoostExecutor)
        loriToolsExecutors.add(ActivePremiumSlotsExecutor)
    }
}
