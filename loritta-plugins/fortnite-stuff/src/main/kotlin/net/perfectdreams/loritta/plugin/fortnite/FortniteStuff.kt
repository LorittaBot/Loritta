package net.perfectdreams.loritta.plugin.fortnite

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import net.perfectdreams.loritta.plugin.fortnite.commands.fortnite.FortniteShopCommand
import net.perfectdreams.loritta.plugin.fortnite.extendedtables.FortniteConfigs
import net.perfectdreams.loritta.plugin.fortnite.extendedtables.FortniteServerConfigs
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class FortniteStuff : LorittaPlugin() {
    var updateStoreItems: UpdateStoreItemsTask? = null

    override fun onEnable() {
        updateStoreItems = UpdateStoreItemsTask(this).apply { start() }

        registerCommand(
                FortniteShopCommand(this)
        )

        transaction(Databases.loritta) {
            SchemaUtils.createMissingTablesAndColumns(
                    FortniteServerConfigs,
                    FortniteConfigs
            )
        }
    }

    override fun onDisable() {
        updateStoreItems?.task?.cancel()
    }
}
