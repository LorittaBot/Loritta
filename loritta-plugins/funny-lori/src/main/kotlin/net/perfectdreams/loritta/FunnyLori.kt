package net.perfectdreams.loritta

import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import net.perfectdreams.loritta.commands.`fun`.RandomSAMCommand
import net.perfectdreams.loritta.commands.`fun`.RandomTikTokCommand

class FunnyLori : LorittaPlugin() {
    override fun onEnable() {
        registerCommand(RandomSAMCommand())
        registerCommand(RandomTikTokCommand())
    }
}