package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.lorituber.LoriTuberItems
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import net.perfectdreams.loritta.lorituber.rpc.packets.BuyGroceryStoreItemRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.BuyGroceryStoreItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.GoToGroceryStoreRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GoToGroceryStoreResponse
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens.GroceryStoreScreen.Companion.groceryStoreItemListViewer
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import java.awt.Color

class GroceryStoreItemScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    private val itemId: String,
) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        val result = sendLoriTuberRPCRequestNew<GoToGroceryStoreResponse>(GoToGroceryStoreRequest(character.id))

        /* val dbStockItems = loritta.transaction {
            // Get the last grocery stock
            val groceryStock = LoriTuberGroceryStocks.selectAll()
                .where {
                    LoriTuberGroceryStocks.shopId eq "lorituber:nelson_grocery_store"
                }
                .orderBy(LoriTuberGroceryStocks.stockedAtTick, SortOrder.DESC)
                .limit(1)
                .first()

            LoriTuberGroceryItems.selectAll()
                .where {
                    LoriTuberGroceryItems.storeStock eq groceryStock[LoriTuberGroceryStocks.id]
                }
                .toList()
        }

        val groceryItems = dbStockItems.map { it[LoriTuberGroceryItems.item] }
            .distinct()
            .map {
                LoriTuberGroceryItem(
                    LoriTuberItems.getById(it),
                    dbStockItems.count { f -> f[LoriTuberGroceryItems.boughtBy] == null && f[LoriTuberGroceryItems.item] == it }
                )
            }

        val groceryItem = LoriTuberGroceryItem(
            LoriTuberItems.getById(itemId),
            dbStockItems.filter { it[LoriTuberGroceryItems.boughtBy] == null }.count { it[LoriTuberGroceryItems.item] == itemId }
        ) */

        when (result) {
            is GoToGroceryStoreResponse.Success -> {
                val groceryItem = result.items.first { it.item.id == itemId }
                val item = net.perfectdreams.loritta.lorituber.items.LoriTuberItems.getById(groceryItem.item)

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

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            /* title = "\uD83D\uDED2 ${groceryItem.item.name}"

                            description = buildString {
                                appendLine(groceryItem.item.description)
                                appendLine()
                                appendLine("**Preço:** ${groceryItem.item.price}")
                                appendLine("**Em estoque:** ${groceryItem.inStock}")
                            } */
                            title = "\uD83D\uDED2 ${item.id}"

                            description = buildString {
                                // appendLine(groceryItem.item.description)
                                appendLine()
                                appendLine("**Preço:** ${item.price}")
                                appendLine("**Em estoque:** ${groceryItem.inStock}")
                            }

                            // image = "https://cdn.discordapp.com/attachments/739823666891849729/1052664743426523227/jerbs_store.png"

                            thumbnail = when {
                                item.id.id == LoriTuberItems.SLICED_BREAD.id -> "https://cdn.discordapp.com/attachments/739823666891849729/1286397821217669212/image.png?ex=66edc2d4&is=66ec7154&hm=d26f15376cd99ad5fd386904a03fb9f86c905291b398c6007fd8348a70314bb0&"
                                item.id.id == LoriTuberItems.STRAWBERRY_YOGURT.id -> "https://cdn.discordapp.com/attachments/358774895850815488/1286732000694370417/image.png?ex=66eefa0f&is=66eda88f&hm=41af550f783f27d5b1852ee67d8683901f29d12308de06b2e7120efc26d334d1&"
                                else -> null
                            }

                            color = Color(255, 172, 51).rgb
                        }

                        actionRow(
                            groceryStoreItemListViewer(
                                command,
                                user,
                                character,
                                result.items,
                                groceryItem
                            )
                        )

                        val buyButton = UnleashedButton.of(
                            ButtonStyle.PRIMARY,
                            "Comprar",
                            null
                        )

                        actionRow(
                            viewMotivesButton,
                            if (groceryItem.inStock == 0)
                                buyButton.asDisabled()
                            else
                                loritta.interactivityManager.buttonForUser(user, buyButton) { context ->
                                    val defer = context.deferEdit()

                                    val buyItemResult = sendLoriTuberRPCRequestNew<BuyGroceryStoreItemResponse>(BuyGroceryStoreItemRequest(character.id, LoriTuberItemId(itemId)))

                                    /* val buyItemResult = loritta.transaction {
                                        // Get the last grocery stock
                                        val groceryStock = LoriTuberGroceryStocks.selectAll()
                                            .where {
                                                LoriTuberGroceryStocks.shopId eq "lorituber:nelson_grocery_store"
                                            }
                                            .orderBy(LoriTuberGroceryStocks.stockedAtTick, SortOrder.DESC)
                                            .limit(1)
                                            .first()

                                        val itemToBeBought = LoriTuberGroceryItems.selectAll()
                                            .where {
                                                LoriTuberGroceryItems.storeStock eq groceryStock[LoriTuberGroceryStocks.id] and (LoriTuberGroceryItems.item eq itemId) and (LoriTuberGroceryItems.boughtBy.isNull())
                                            }
                                            .limit(1)
                                            .firstOrNull()

                                        if (itemToBeBought == null)
                                            return@transaction BuyItemResult.NotInStock

                                        val now = Instant.now()

                                        LoriTuberGroceryItems.update({ LoriTuberGroceryItems.id eq itemToBeBought[LoriTuberGroceryItems.id] }) {
                                            it[LoriTuberGroceryItems.boughtBy] = character.id
                                            it[LoriTuberGroceryItems.boughtAt] = now
                                        }

                                        LoriTuberCharacterInventoryItems.insert {
                                            it[LoriTuberCharacterInventoryItems.owner] = character.id
                                            it[LoriTuberCharacterInventoryItems.item] = itemId
                                            it[LoriTuberCharacterInventoryItems.addedAt] = now
                                        }

                                        return@transaction BuyItemResult.Success
                                    } */

                                    when (buyItemResult) {
                                        BuyGroceryStoreItemResponse.NotInStock -> {
                                            context.reply(true) {
                                                styled(
                                                    "Item fora de estoque!"
                                                )
                                            }

                                            command.switchScreen(
                                                GroceryStoreItemScreen(
                                                    command,
                                                    user,
                                                    defer,
                                                    character,
                                                    itemId
                                                )
                                            )
                                        }
                                        BuyGroceryStoreItemResponse.Closed -> {
                                            context.reply(true) {
                                                styled(
                                                    "Fechado!"
                                                )
                                            }

                                            command.switchScreen(
                                                GroceryStoreItemScreen(
                                                    command,
                                                    user,
                                                    defer,
                                                    character,
                                                    itemId
                                                )
                                            )
                                        }
                                        BuyGroceryStoreItemResponse.Success -> {
                                            context.reply(true) {
                                                styled(
                                                    "Item comprado!"
                                                )
                                            }

                                            command.switchScreen(
                                                GroceryStoreItemScreen(
                                                    command,
                                                    user,
                                                    defer,
                                                    character,
                                                    itemId
                                                )
                                            )
                                        }
                                    }
                                }
                        )
                    }
                ).setReplace(true).await()
            }
            GoToGroceryStoreResponse.Closed -> TODO()
        }
    }

    sealed class BuyItemResult {
        data object NotInStock : BuyItemResult()
        data object Success : BuyItemResult()
    }
}