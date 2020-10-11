package net.perfectdreams.loritta.plugin.fortnite

import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.fortnite.commands.fortnite.*
import net.perfectdreams.loritta.plugin.fortnite.routes.ConfigureFortniteRoute
import net.perfectdreams.loritta.plugin.fortnite.routes.PostItemListRoute
import net.perfectdreams.loritta.plugin.fortnite.routes.PostShopUpdateRoute
import net.perfectdreams.loritta.plugin.fortnite.tables.FakeTable
import net.perfectdreams.loritta.plugin.fortnite.tables.FortniteConfigs
import net.perfectdreams.loritta.plugin.fortnite.tables.TrackedFortniteItems
import net.perfectdreams.loritta.plugin.fortnite.transformers.FortniteConfigTransformer
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

class FortniteStuff(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
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

        var forceNewBroadcast = false
    }

    var updateStoreItems: UpdateStoreItemsTask? = null
    val storeFileNamesByLocaleId = mutableMapOf<String, String>()
    val itemsInfo = mutableMapOf<String, JsonArray>()

    override fun onEnable() {
        updateStoreItems = UpdateStoreItemsTask(this).apply { start() }

        registerCommands(
                FortniteItemCommand(this),
                FortniteNewsCommand(this),
                FortniteShopCommand(this),
                FortniteNotifyCommand(this),
                FortniteStatsCommand(this)
        )

        loriToolsExecutors.add(ForceShopUpdateExecutor)
        configTransformers.add(FortniteConfigTransformer)
        routes.add(PostShopUpdateRoute(this, loritta))
        routes.add(PostItemListRoute(this, loritta))
        routes.add(ConfigureFortniteRoute(loritta))

        // acessar qualquer coisa só para registrar corretamente
        FakeTable.fortniteConfig
        transaction(Databases.loritta) {
            SchemaUtils.createMissingTablesAndColumns(
                    ServerConfigs,
                    FortniteConfigs,
                    TrackedFortniteItems
            )
        }
    }
}
