package net.perfectdreams.loritta.plugin.loriguildstuff

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.loriguildstuff.commands.AddBackgroundCommand
import net.perfectdreams.loritta.plugin.loriguildstuff.commands.NotifyBackgroundsCommand
import net.perfectdreams.loritta.plugin.loriguildstuff.commands.NotifyCommand
import net.perfectdreams.loritta.plugin.loriguildstuff.modules.AddReactionsToChannelsModule

class LoriGuildStuffPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
    override fun onEnable() {
        loritta as LorittaDiscord
        registerCommand(NotifyBackgroundsCommand.create(loritta))
        registerCommand(NotifyCommand.create(loritta))
        registerCommand(AddBackgroundCommand.create(loritta))

        addMessageReceivedModules(AddReactionsToChannelsModule(this))
    }
}