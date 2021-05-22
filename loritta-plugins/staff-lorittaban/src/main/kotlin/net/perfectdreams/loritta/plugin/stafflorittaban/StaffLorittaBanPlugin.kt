package net.perfectdreams.loritta.plugin.stafflorittaban

import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.stafflorittaban.listeners.CheckReactionsForLoriBanListener
import net.perfectdreams.loritta.plugin.stafflorittaban.listeners.CheckReactionsForPrivateSpamListener
import net.perfectdreams.loritta.plugin.stafflorittaban.modules.AddReactionForStaffLoriBanModule
import net.perfectdreams.loritta.utils.HoconUtils.decodeFromFile
import java.io.File

class StaffLorittaBanPlugin(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onEnable() {
        super.onEnable()

        val config = Constants.HOCON.decodeFromFile<StaffLorittaBanConfig>(File(dataFolder, "config.conf"))

        addMessageReceivedModule(AddReactionForStaffLoriBanModule(config))
        addEventListener(CheckReactionsForLoriBanListener(config))
        addEventListener(CheckReactionsForPrivateSpamListener(config))
    }
}
