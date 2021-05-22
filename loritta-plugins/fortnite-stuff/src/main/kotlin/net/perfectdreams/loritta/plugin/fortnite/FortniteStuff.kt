package net.perfectdreams.loritta.plugin.fortnite

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.fortnite.commands.fortnite.*
import net.perfectdreams.loritta.plugin.fortnite.routes.ConfigureFortniteRoute
import net.perfectdreams.loritta.plugin.fortnite.routes.PostItemListRoute
import net.perfectdreams.loritta.plugin.fortnite.routes.PostShopUpdateRoute
import net.perfectdreams.loritta.plugin.fortnite.tables.FakeTable
import net.perfectdreams.loritta.plugin.fortnite.tables.FortniteConfigs
import net.perfectdreams.loritta.plugin.fortnite.tables.TrackedFortniteItems
import net.perfectdreams.loritta.plugin.fortnite.transformers.FortniteConfigTransformer
import net.perfectdreams.loritta.utils.Emotes
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

        /**
         * Gets a Fortnite Item by Name or ID
         *
         * @param m instance of the [FortniteStuff] class
         * @param locale the locale that the items should be in
         * @param context command context
         * @param name the name or ID of the item
         * @param onSuccess when a item is found, this callback will be invoked with the Fortnite Item Element and a maybe null message (used when there is multiple items)
         * @param onFailure when no item matched the [name]
         */
        suspend fun getFortniteItemByName(
            m: FortniteStuff,
            locale: BaseLocale,
            context: DiscordCommandContext,
            name: String,
            onSuccess: suspend (JsonElement, Message?) -> (Unit),
            onFailure: suspend () -> (Unit)
        ) {
            val items = m.itemsInfo.values.flatMap {
                it.filter {
                    it["id"].string == name || it["name"].nullString?.contains(name, true) == true
                }
            }.distinctBy { it["id"].string }

            val fortniteItemsInCurrentLocale = m.itemsInfo[locale["commands.command.fnshop.localeId"]]!!

            val embed = EmbedBuilder()

            when {
                items.size == 1 -> {
                    // Pegar na linguagem do usuário
                    onSuccess.invoke(
                            fortniteItemsInCurrentLocale.first { it["id"].string == items.first()["id"].string },
                            null
                    )
                }
                items.isNotEmpty() -> {
                    for (i in 0 until Math.min(9, items.size)) {
                        val item = items[i].obj
                        val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { item["id"].string == it["id"].string }.obj

                        embed.setTitle("${Emotes.LORI_HM} ${locale["commands.command.fnitem.multipleItems"]}")
                        embed.setColor(Color(0, 125, 187))
                        embed.appendDescription("${Constants.INDEXES[i]} ${fortniteItemInCurrentLocale["name"].nullString} (${fortniteItemInCurrentLocale["type"]["value"].nullString})\n")
                    }

                    val result = context.sendMessage(embed.build())

                    result.onReactionAddByAuthor(context) {
                        val idx = Constants.INDEXES.indexOf(it.reactionEmote.name)

                        // Caso seja uma reaçõa inválida ou que não tem no metadata, ignore!
                        if (idx == -1 || (idx + 1) > items.size)
                            return@onReactionAddByAuthor

                        val item = items[idx]
                        val fortniteItemInCurrentLocale = fortniteItemsInCurrentLocale.first { item["id"].string == it["id"].string }

                        onSuccess.invoke(fortniteItemInCurrentLocale, result)
                    }

                    // Adicionar os reactions
                    for (i in 0 until Math.min(9, items.size)) {
                        result.addReaction(Constants.INDEXES[i]).queue()
                    }
                }
                else -> onFailure.invoke()
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
