package net.perfectdreams.loritta.plugin.stafflorittaban

import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.stafflorittaban.listeners.CheckReactionsForLoriBanListener
import net.perfectdreams.loritta.plugin.stafflorittaban.modules.AddReactionForStaffLoriBanModule
import java.io.File

class StaffLorittaBanPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onEnable() {
        super.onEnable()

        val config = Constants.HOCON_MAPPER.readValue<StaffLorittaBanConfig>(File(dataFolder, "config.conf"))

        addMessageReceivedModule(AddReactionForStaffLoriBanModule(config))
        addEventListener(CheckReactionsForLoriBanListener(config))
    }
}
