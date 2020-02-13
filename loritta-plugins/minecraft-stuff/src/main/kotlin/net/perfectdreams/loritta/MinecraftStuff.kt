package net.perfectdreams.loritta

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.commands.minecraft.McSignCommand

class MinecraftStuff(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {
    override fun onEnable() {
        registerCommand(McSignCommand.command(loritta))
    }
}