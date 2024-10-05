package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.lorituber.bhav.ItemActionOption
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import java.awt.Color
import java.util.*

class PrepareFoodScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val fridgeLocalId: UUID,
    val response: CharacterUseItemResponse.Success.Fridge.PrepareFoodMenu,
    val selectedItems: List<LoriTuberItemId>
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

        // TODO: Remove this (migrated to use object interaction packets)
        // val response = sendLoriTuberRPCRequestNew<PrepareCraftingResponse>(PrepareCraftingRequest(character.id))
        val items = response.inventory

        hook.editOriginal(
            MessageEdit {
                embed {
                    title = "Preparar Comida"

                    description = buildString {
                        appendLine("Faça uma gororoba!")
                        appendLine()
                        for (item in selectedItems) {
                            appendLine(item)
                        }
                    }

                    // Can't create a select menu without any items
                    if (items.isNotEmpty()) {
                        actionRow(
                            loritta.interactivityManager.stringSelectMenuForUser(
                                user,
                                {
                                    this.minValues = 2
                                    this.maxValues = 3

                                    for (item in items) {
                                        addOption("${item.id} [${item.quantity}x]", item.id.id.toString())
                                    }

                                    this.setDefaultValues(selectedItems.map { it.id })
                                }
                            ) { context, values ->
                                val selectedItems = items.filter {
                                    it.id.id in values
                                }

                                command.switchScreen(
                                    PrepareFoodScreen(
                                        command,
                                        user,
                                        context.deferEdit(),
                                        character,
                                        fridgeLocalId,
                                        response,
                                        selectedItems.map { it.id }
                                    )
                                )
                            }
                        )
                    }

                    color = Color(255, 172, 51).rgb
                }

                val prepareButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    "Preparar Comida"
                )

                actionRow(
                    viewMotivesButton,
                    if (selectedItems.size >= 2) {
                        loritta.interactivityManager.buttonForUser(
                            user,
                            prepareButton
                        ) { context ->
                            val defer = context.deferEdit()

                            // TODO: Remove this (migrated to use object interaction packets)
                            // val response = sendLoriTuberRPCRequestNew<StartCraftingResponse>(StartCraftingRequest(character.id, selectedItems))
                            val response = sendLoriTuberRPCRequestNew<CharacterUseItemResponse>(
                                CharacterUseItemRequest(
                                    character.id,
                                    fridgeLocalId,
                                    ItemActionOption.PrepareFood(selectedItems)
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
                    } else prepareButton.asDisabled()
                )
            }
        ).setReplace(true).await()
    }
}