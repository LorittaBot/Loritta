package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberGroceryItems
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberGroceryStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberServerInfos
import net.perfectdreams.loritta.lorituber.*
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import java.awt.Color

class GroceryStoreScreen(command: LoriTuberCommand, user: User, hook: InteractionHook, val character: LoriTuberCommand.PlayerCharacter) : LoriTuberScreen(command, user, hook) {
    companion object {
        fun groceryStoreItemListViewer(
            command: LoriTuberCommand,
            user: User,
            character: LoriTuberCommand.PlayerCharacter,
            groceryItems: List<LoriTuberGroceryItem>,
            viewingItem: LoriTuberGroceryItem?
        ): StringSelectMenu {
            val loritta = command.loritta

            return loritta.interactivityManager.stringSelectMenuForUser(
                user,
                {
                    addOption("Mercearia do Nelson", "store_overview")

                    for (groceryItem in groceryItems) {
                        addOption("${groceryItem.item.name} [${groceryItem.inStock}x]", groceryItem.item.id)
                    }

                    setDefaultValues(viewingItem?.item?.id ?: "store_overview")
                }
            ) { context, args ->
                val defer = context.deferEdit()

                if (args[0] == "store_overview") {
                    command.switchScreen(
                        GroceryStoreScreen(
                            command,
                            user,
                            defer,
                            character
                        )
                    )
                    return@stringSelectMenuForUser
                }

                command.switchScreen(
                    GroceryStoreItemScreen(
                        command,
                        user,
                        defer,
                        character,
                        args[0]
                    )
                )
            }
        }
    }

    override suspend fun render() {
        val result = loritta.transaction {
            val serverInfo = LoriTuberServerInfos.selectAll()
                .where { LoriTuberServerInfos.type eq LoriTuberServer.GENERAL_INFO_KEY }
                .first()
                .get(LoriTuberServerInfos.data)
                .let { Json.decodeFromString<ServerInfo>(it) }

            val worldTime = WorldTime(serverInfo.currentTick)

            // Let's bypass for now
            if (false && worldTime.hours !in 7..18) {
                return@transaction GoToGroceryStoreResult.Closed
            }

            // Get the last grocery stock
            val groceryStock = LoriTuberGroceryStocks.selectAll()
                .where {
                    LoriTuberGroceryStocks.shopId eq "lorituber:nelson_grocery_store"
                }
                .orderBy(LoriTuberGroceryStocks.stockedAtTick, SortOrder.DESC)
                .limit(1)
                .first()

            val items = LoriTuberGroceryItems.selectAll()
                .where {
                    LoriTuberGroceryItems.storeStock eq groceryStock[LoriTuberGroceryStocks.id]
                }
                .toList()

            return@transaction GoToGroceryStoreResult.Success(items)
        }

        val viewMotivesButton = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.PRIMARY,
            "Voltar ao cafofo",
            {
                emoji = Emoji.fromUnicode("\uD83C\uDFE0")
            }
        ) {
            command.switchScreen(
                ViewMotivesScreen(
                    command,
                    user,
                    it.deferEdit(),
                    character
                )
            )
        }

        when (result) {
            is GoToGroceryStoreResult.Success -> {
                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "\uD83D\uDED2 Mercearia do Nelson"

                            description = """A Mercearia do Nelson tem coisas muito legais para você comprar hmm delisia"""

                            // image = "https://cdn.discordapp.com/attachments/739823666891849729/1052664743426523227/jerbs_store.png"

                            color = Color(255, 172, 51).rgb
                        }

                        val groceryItems = result.items.map { it[LoriTuberGroceryItems.item] }
                            .distinct()
                            .map {
                                LoriTuberGroceryItem(
                                    LoriTuberItems.getById(it),
                                    result.items.count { f -> f[LoriTuberGroceryItems.boughtBy] == null && f[LoriTuberGroceryItems.item] == it }
                                )
                            }

                        actionRow(
                            groceryStoreItemListViewer(
                                command,
                                user,
                                character,
                                groceryItems,
                                null
                            )
                        )

                        actionRow(viewMotivesButton)
                    }
                ).setReplace(true).await()
            }
            GoToGroceryStoreResult.Closed -> {
                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "\uD83D\uDED2 Mercearia do Nelson"

                            description = """A Mercearia do Nelson só está aberta as 07:00 - 19:00!"""

                            // image = "https://cdn.discordapp.com/attachments/739823666891849729/1052664743426523227/jerbs_store.png"

                            color = Color(255, 172, 51).rgb
                        }

                        actionRow(viewMotivesButton)
                    }
                ).setReplace(true).await()
            }
        }
    }

    sealed class GoToGroceryStoreResult {
        data class Success(val items: List<ResultRow>) : GoToGroceryStoreResult()
        data object Closed : GoToGroceryStoreResult()
    }
}