package net.perfectdreams.loritta.plugin.loriguildstuff

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.loriguildstuff.commands.*
import net.perfectdreams.loritta.plugin.loriguildstuff.modules.AddReactionsToChannelsModule
import net.perfectdreams.loritta.plugin.loriguildstuff.modules.BlockBadWordsModule
import java.io.File

class LoriGuildStuffPlugin(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
    val badWords by lazy { File(dataFolder, "bad-words.txt").readLines() }

    override fun onEnable() {
        registerCommands(
                NotifyBackgroundsCommand.create(loritta),
                NotifyCommand.create(loritta),
                AddBackgroundCommand.create(loritta),
                ColorCommand.create(loritta),
                SendFanartCommand.create(loritta),
                FastBanCommand.create(loritta),
                SendFanartCommand.create(loritta)
        )

        addMessageReceivedModules(AddReactionsToChannelsModule(this))
        addMessageReceivedModules(BlockBadWordsModule(this))
    }
}