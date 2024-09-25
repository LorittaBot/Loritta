package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberCharacterInventoryItems
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberCharacters
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberServerInfos
import net.perfectdreams.loritta.lorituber.LoriTuberItem
import net.perfectdreams.loritta.lorituber.LoriTuberItems
import net.perfectdreams.loritta.lorituber.LoriTuberServer
import net.perfectdreams.loritta.lorituber.ServerInfo
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.awt.Color

class EatFoodScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val selectedFood: LoriTuberItem?
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

        val items = loritta.transaction {
            LoriTuberCharacterInventoryItems
                .selectAll()
                .where {
                    LoriTuberCharacterInventoryItems.owner eq character.id
                }
                .toList()
                .groupBy { it[LoriTuberCharacterInventoryItems.item] }
                .map {
                    LoriTuberItems.getById(it.key) to it.value.size
                }
        }

        val foods = items.filter { it.first.foodAttributes != null }

        hook.editOriginal(
            MessageEdit {
                embed {
                    title = "Comer Comida"

                    description = buildString {
                        appendLine("hmm delisia")

                        if (selectedFood != null) {
                            appendLine()
                            appendLine(selectedFood.description)
                        }
                    }

                    // Can't create a select menu without any items
                    if (foods.isNotEmpty()) {
                        actionRow(
                            loritta.interactivityManager.stringSelectMenuForUser(
                                user,
                                {
                                    for (item in items) {
                                        addOption("${item.first.name} [${item.second}x]", item.first.id)
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
                                        LoriTuberItems.getById(values[0])
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

                                loritta.transaction {
                                    val serverInfo = loritta.transaction {
                                        LoriTuberServerInfos.selectAll()
                                            .where { LoriTuberServerInfos.type eq LoriTuberServer.GENERAL_INFO_KEY }
                                            .first()
                                            .get(LoriTuberServerInfos.data)
                                            .let { Json.decodeFromString<ServerInfo>(it) }
                                    }

                                    // Attempt to remove the item from the player inventory
                                    val itemToBeDeleted = LoriTuberCharacterInventoryItems.selectAll()
                                        .where {
                                            LoriTuberCharacterInventoryItems.item eq selectedFood.id and (LoriTuberCharacterInventoryItems.owner eq character.id)
                                        }
                                        .limit(1)
                                        .firstOrNull()

                                    // Whoops, you don't actually have the item!
                                    if (itemToBeDeleted == null)
                                        return@transaction EatFoodResult.ItemNotFound

                                    LoriTuberCharacterInventoryItems.deleteWhere { LoriTuberCharacterInventoryItems.id eq itemToBeDeleted[LoriTuberCharacterInventoryItems.id] }

                                    LoriTuberCharacters.update({ LoriTuberCharacters.id eq character.id }) {
                                        it[LoriTuberCharacters.currentTask] = Json.encodeToString<LoriTuberTask>(LoriTuberTask.Eating(selectedFood.id, serverInfo.currentTick))
                                    }
                                }

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