package net.perfectdreams.loritta

import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.listeners.AddReactionListener
import net.perfectdreams.loritta.modules.QuirkyModule
import net.perfectdreams.loritta.modules.ThankYouLoriModule
import net.perfectdreams.loritta.platform.discord.plugin.DiscordPlugin
import java.io.File

class QuirkyStuff : DiscordPlugin() {
    override fun onEnable() {
        val config = Constants.HOCON_MAPPER.readValue<QuirkyConfig>(File(dataFolder, "config.conf"))

        registerEventListeners(
                AddReactionListener()
        )

        registerMessageReceivedModules(
                QuirkyModule(config),
                ThankYouLoriModule(config)
        )
    }
}