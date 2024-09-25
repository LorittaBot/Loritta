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
import net.perfectdreams.loritta.lorituber.*
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.awt.Color

class PrepareFoodScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val selectedItems: List<LoriTuberItem>
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

        hook.editOriginal(
            MessageEdit {
                embed {
                    title = "Preparar Comida"

                    description = buildString {
                        appendLine("Faça uma gororoba!")
                        appendLine()
                        for (item in selectedItems) {
                            appendLine(item.name)
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
                                        addOption("${item.first.name} [${item.second}x]", item.first.id)
                                    }

                                    this.setDefaultValues(selectedItems.map { it.id })
                                }
                            ) { context, values ->
                                val selectedItems = LoriTuberItems.allItems
                                    .filter {
                                        it.id in values
                                    }

                                command.switchScreen(
                                    PrepareFoodScreen(
                                        command,
                                        user,
                                        context.deferEdit(),
                                        character,
                                        selectedItems
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

                            val matchedRecipe = LoriTuberRecipes.getMatchingRecipeForItems(selectedItems)

                            loritta.transaction {
                                val serverInfo = loritta.transaction {
                                    LoriTuberServerInfos.selectAll()
                                        .where { LoriTuberServerInfos.type eq LoriTuberServer.GENERAL_INFO_KEY }
                                        .first()
                                        .get(LoriTuberServerInfos.data)
                                        .let { Json.decodeFromString<ServerInfo>(it) }
                                }

                                LoriTuberCharacters.update({ LoriTuberCharacters.id eq character.id }) {
                                    it[LoriTuberCharacters.currentTask] = Json.encodeToString<LoriTuberTask>(LoriTuberTask.PreparingFood(matchedRecipe?.id, selectedItems.map { it.id }, serverInfo.currentTick))
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
                    } else prepareButton.asDisabled()
                )
            }
        ).setReplace(true).await()
    }
}