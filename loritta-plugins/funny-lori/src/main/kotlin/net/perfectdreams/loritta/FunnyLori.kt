package net.perfectdreams.loritta

import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import net.perfectdreams.loritta.commands.`fun`.RandomSAMCommand
import net.perfectdreams.loritta.commands.`fun`.RandomTikTokCommand

class FunnyLori : LorittaPlugin() {
    override fun onEnable() {
        registerCommand(RandomSAMCommand())
        if (loritta.config.loritta.environment == EnvironmentType.CANARY)
            registerCommand(RandomTikTokCommand())
    }
}