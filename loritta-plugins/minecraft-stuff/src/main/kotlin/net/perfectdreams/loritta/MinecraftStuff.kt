package net.perfectdreams.loritta

import net.perfectdreams.loritta.commands.minecraft.McSignCommand
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin

class MinecraftStuff(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
    override fun onEnable() {
        registerCommand(McSignCommand(this))
    }
}