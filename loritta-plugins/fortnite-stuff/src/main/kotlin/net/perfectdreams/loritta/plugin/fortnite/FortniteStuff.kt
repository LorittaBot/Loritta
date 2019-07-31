package net.perfectdreams.loritta.plugin.fortnite

import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import net.perfectdreams.loritta.plugin.fortnite.commands.fortnite.FortniteShopCommand

class FortniteStuff : LorittaPlugin() {
    var updateStoreItems: UpdateStoreItemsTask? = null

    override fun onEnable() {
        updateStoreItems = UpdateStoreItemsTask(this).apply { start() }

        registerCommand(
                FortniteShopCommand(this)
        )
    }

    override fun onDisable() {
        updateStoreItems?.task?.cancel()
    }
}
