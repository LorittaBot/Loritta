package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.items.LoriTuberGroceryItemData
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import net.perfectdreams.loritta.lorituber.rpc.packets.GoToGroceryStoreRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GoToGroceryStoreResponse
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import org.jetbrains.exposed.sql.ResultRow
import java.util.*

class GroceryStoreScreen(command: LoriTuberCommand, user: User, hook: InteractionHook, val character: LoriTuberCommand.PlayerCharacter) : LoriTuberScreen(command, user, hook) {
    companion object {
        fun groceryStoreItemListViewer(
            command: LoriTuberCommand,
            user: User,
            character: LoriTuberCommand.PlayerCharacter,
            browseStoreItemsOption: ObjectActionOption.BrowseStoreItems,
            groceryItems: List<LoriTuberGroceryItemData>,
            itemStoreLocalId: UUID,
            viewingItem: LoriTuberGroceryItemData?
        ): StringSelectMenu {
            val loritta = command.loritta

            return loritta.interactivityManager.stringSelectMenuForUser(
                user,
                {
                    addOption("Mercearia do Nelson", "store_overview")

                    for (groceryItem in groceryItems) {
                        addOption("${groceryItem.item.id} [${groceryItem.inStock}x]", groceryItem.item.id)
                    }

                    setDefaultValues(viewingItem?.item?.id ?: "store_overview")
                }
            ) { context, args ->
                val defer = context.deferEdit()

                if (args[0] == "store_overview") {
                    command.switchScreen(
                        GroceryStoreItemScreen(
                            command,
                            user,
                            defer,
                            character,
                            browseStoreItemsOption,
                            itemStoreLocalId,
                            null
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
                        browseStoreItemsOption,
                        itemStoreLocalId,
                        LoriTuberItemId(args[0])
                    )
                )
            }
        }
    }

    override suspend fun render() {
        val result = sendLoriTuberRPCRequestNew<GoToGroceryStoreResponse>(GoToGroceryStoreRequest(character.id))

        /* val result = loritta.transaction {
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
        } */

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

        /* when (result) {
            is GoToGroceryStoreResponse.Success -> {
                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "\uD83D\uDED2 Mercearia do Nelson"

                            description = """A Mercearia do Nelson tem coisas muito legais para você comprar hmm delisia"""

                            // image = "https://cdn.discordapp.com/attachments/739823666891849729/1052664743426523227/jerbs_store.png"

                            color = Color(255, 172, 51).rgb
                        }

                        actionRow(
                            groceryStoreItemListViewer(
                                command,
                                user,
                                character,
                                result.items,
                                TODO(),
                                TODO()
                            )
                        )

                        actionRow(viewMotivesButton)
                    }
                ).setReplace(true).await()
            }
            GoToGroceryStoreResponse.Closed -> {
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
        } */
    }

    sealed class GoToGroceryStoreResult {
        data class Success(val items: List<ResultRow>) : GoToGroceryStoreResult()
        data object Closed : GoToGroceryStoreResult()
    }
}