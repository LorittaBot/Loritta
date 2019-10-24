package net.perfectdreams.loritta.plugin.fortnite

import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import net.perfectdreams.loritta.plugin.fortnite.commands.fortnite.*
import net.perfectdreams.loritta.plugin.fortnite.extendedtables.FortniteConfigs
import net.perfectdreams.loritta.plugin.fortnite.extendedtables.FortniteServerConfigs
import net.perfectdreams.loritta.plugin.fortnite.extendedtables.TrackedFortniteItems
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

class FortniteStuff : LorittaPlugin() {
    companion object {
        fun convertRarityToColor(type: String): Color {
            return when (type) {
                "uncommon" -> Color(64, 136, 1)
                "rare" -> Color(0, 125, 187)
                "epic" -> Color(151, 60, 195)
                "legendary" -> Color(195, 119, 58)
                "marvel" -> Color(213, 186, 99)
                "creator" -> Color(21, 131, 135)
                "dc" -> Color(43, 61, 88)
                "dark" -> Color(159, 0, 120)
                else -> Color(176, 176, 150)
            }
        }
    }

    var updateStoreItems: UpdateStoreItemsTask? = null
    val itemsInfo = mutableMapOf<String, JsonArray>()

    override fun onEnable() {
        updateStoreItems = UpdateStoreItemsTask(this).apply { start() }

        registerCommand(
                FortniteShopCommand(this)
        )

        registerCommand(
                FortniteItemCommand(this)
        )

        registerCommand(
                FortniteNewsCommand(this)
        )

        registerCommand(
                FortniteStatsCommand(this)
        )

        registerCommand(
                FortniteNotifyCommand(this)
        )

        transaction(Databases.loritta) {
            SchemaUtils.createMissingTablesAndColumns(
                    FortniteServerConfigs,
                    FortniteConfigs,
                    TrackedFortniteItems
            )
        }
    }

    override fun onDisable() {
        updateStoreItems?.task?.cancel()
    }
}
