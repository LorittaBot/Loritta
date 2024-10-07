package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import net.perfectdreams.loritta.lorituber.items.LoriTuberItems
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import java.awt.Color

class EatFoodScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val response: CharacterUseItemResponse.Success.Fridge.EatFoodMenu,
    val selectedFood: LoriTuberItemId?
) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
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

        data class ItemWrapper(
            val item: net.perfectdreams.loritta.lorituber.items.LoriTuberItem,
            val quantity: Int
        )

        // TODO: Remove this (migrated to use object interaction packets)
        // val response = sendLoriTuberRPCRequestNew<SelectFoodMenuResponse>(SelectFoodMenuRequest(character.id))
        val items = response.inventory.map { ItemWrapper(LoriTuberItems.getById(it.id), it.quantity) }
        val foods = items.filter { it.item.foodAttributes != null }

        hook.editOriginal(
            MessageEdit {
                embed {
                    title = "Comer Comida"

                    description = buildString {
                        appendLine("hmm delisia")

                        if (selectedFood != null) {
                            appendLine()
                            // TODO: Description later
                            // appendLine(selectedFood.description)
                        }
                    }

                    // Can't create a select menu without any items
                    if (foods.isNotEmpty()) {
                        actionRow(
                            loritta.interactivityManager.stringSelectMenuForUser(
                                user,
                                {
                                    for (item in items) {
                                        addOption("${item.item.id} [${item.quantity}x]", item.item.id.id)
                                    }

                                    if (selectedFood != null)
                                        setDefaultValues(selectedFood.id)
                                }
                            ) { context, values ->
                                val defer = context.deferEdit()

                                command.switchScreen(
                                    EatFoodScreen(
                                        command,
                                        user,
                                        defer,
                                        character,
                                        response,
                                        foods.first { it.item.id.id == values[0] }.item.id
                                    )
                                )
                            }
                        )
                    }

                    val eatButton = UnleashedButton.of(
                        ButtonStyle.PRIMARY,
                        "Comer"
                    )

                    if (selectedFood != null) {
                        actionRow(
                            loritta.interactivityManager.buttonForUser(
                                user,
                                eatButton
                            ) { context ->
                                val defer = context.deferEdit()

                                // TODO: Remove this (migrated to use object interaction packets)
                                /* val response = sendLoriTuberRPCRequestNew<EatFoodResponse>(EatFoodRequest(character.id, selectedFood))
                                when (response) {
                                    EatFoodResponse.ItemNotEdible -> TODO()
                                    EatFoodResponse.ItemNotFound -> TODO()
                                    EatFoodResponse.Success -> {
                                        command.switchScreen(
                                            ViewMotivesScreen(
                                                command,
                                                user,
                                                defer,
                                                character
                                            )
                                        )
                                    }
                                } */
                                val matchedLocalId = response.inventory.first { it.id == selectedFood }

                                val response = sendLoriTuberRPCRequestNew<CharacterUseItemResponse>(
                                    CharacterUseItemRequest(
                                        character.id,
                                        matchedLocalId.localId,
                                        ObjectActionOption.EatFood
                                    )
                                )

                                command.switchScreen(
                                    ViewMotivesScreen(
                                        command,
                                        user,
                                        defer,
                                        character
                                    )
                                )
                            }
                        )
                    } else {
                        actionRow(
                            eatButton.asDisabled()
                        )
                    }

                    color = Color(255, 172, 51).rgb
                }

                actionRow(viewMotivesButton)
            }
        ).setReplace(true).await()
    }

    sealed class EatFoodResult {
        data object ItemNotFound : EatFoodResult()
        data object Success : EatFoodResult()
    }
}