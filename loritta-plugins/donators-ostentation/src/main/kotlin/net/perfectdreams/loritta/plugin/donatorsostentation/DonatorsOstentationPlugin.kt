package net.perfectdreams.loritta.plugin.donatorsostentation

import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.donatorsostentation.commands.ActivePremiumSlotsExecutor
import net.perfectdreams.loritta.plugin.donatorsostentation.commands.DisableBoostExecutor
import net.perfectdreams.loritta.plugin.donatorsostentation.commands.EnableBoostExecutor
import net.perfectdreams.loritta.plugin.donatorsostentation.listeners.BoostGuildListener
import net.perfectdreams.loritta.plugin.donatorsostentation.modules.CheckBoostStatusModule
import java.io.File

class DonatorsOstentationPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onEnable() {
        super.onEnable()

        val config = Constants.HOCON_MAPPER.readValue<DonatorsOstentationConfig>(File(dataFolder, "config.conf"))

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
