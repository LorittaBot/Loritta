package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.lorituber.LoriTuberUtils
import net.perfectdreams.loritta.lorituber.WorldTime
import net.perfectdreams.loritta.lorituber.items.LoriTuberItems
import net.perfectdreams.loritta.lorituber.recipes.LoriTuberRecipes
import net.perfectdreams.loritta.lorituber.rpc.packets.*
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask
import java.awt.Color

class ViewMotivesScreen(command: LoriTuberCommand, user: User, hook: InteractionHook, val character: LoriTuberCommand.PlayerCharacter) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        if (command.checkMail(user, hook, character, this))
            return

        val characterStatus = sendLoriTuberRPCRequestNew<ViewCharacterMotivesResponse>(ViewCharacterMotivesRequest(character.id))

        /* val characterStatus = loritta.transaction {
            val serverInfo = loritta.transaction {
                LoriTuberServerInfos.selectAll()
                    .where { LoriTuberServerInfos.type eq LoriTuberServer.GENERAL_INFO_KEY }
                    .first()
                    .get(LoriTuberServerInfos.data)
                    .let { Json.decodeFromString<ServerInfo>(it) }
            }

            val character = LoriTuberCharacters.selectAll().where {
                LoriTuberCharacters.id eq character.id
            }.first()

            return@transaction GetCharacterStatusResult(
                serverInfo.currentTick,
                character[LoriTuberCharacters.name],
                character[LoriTuberCharacters.energyNeed],
                character[LoriTuberCharacters.hungerNeed],
                character[LoriTuberCharacters.funNeed],
                character[LoriTuberCharacters.hygieneNeed],
                character[LoriTuberCharacters.bladderNeed],
                character[LoriTuberCharacters.socialNeed],
                character[LoriTuberCharacters.currentTask]?.let { Json.decodeFromString<LoriTuberTask>(it) }
            )
        } */

        val button = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.PRIMARY,
            "Ver Canal no LoriTube",
            {
                emoji = Emoji.fromUnicode("\uD83C\uDF9E️")
            }
        ) {
            val channel = sendLoriTuberRPCRequestNew<GetChannelsByCharacterResponse>(GetChannelsByCharacterRequest(character.id))
                .channels
                .firstOrNull()

            if (channel != null) {
                command.switchScreen(
                    ViewChannelScreen(
                        command,
                        user,
                        it.deferEdit(),
                        character,
                        channel.id
                    )
                )
            } else {
                command.switchScreen(
                    CreateChannelScreen(
                        command,
                        user,
                        it.deferEdit(),
                        character
                    )
                )
            }
        }

        val cancelActionButton = UnleashedButton.of(
            ButtonStyle.DANGER,
            "Cancelar Ação Atual"
        )

        val sleep = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.PRIMARY,
            "Dormir",
            {
                emoji = Emoji.fromUnicode("\uD83D\uDE34")
            }
        ) {
            sendLoriTuberRPCRequestNew<SetCharacterSleepingResponse>(SetCharacterSleepingRequest(character.id))

            this.hook = it.deferEdit()
            command.switchScreen(this)
            /* when (sendLoriTuberRPCRequest<StartTaskResponse>(StartTaskRequest(character.id, LoriTuberTask.Sleeping()))) {
                is StartTaskResponse.Success -> {
                    this.hook = it.deferEdit()
                    command.switchScreen(this)
                }
                is StartTaskResponse.CharacterIsAlreadyDoingAnotherTask -> it.deferEdit().editOriginal(
                    MessageEdit {
                        content = "Você já está fazendo outra tarefa!"
                    }
                ).setReplace(true).await()
            } */
            // createUI(user, it.deferEdit(), LoriTuberCommand.LoriTuberScreen.Sleeping(screen.character))
        }

        val eatFood = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.PRIMARY,
            "Comer",
            {
                emoji = Emoji.fromUnicode("\uD83C\uDF7D\uFE0F")
            }
        ) {
            command.switchScreen(
                EatFoodScreen(
                    command,
                    user,
                    it.deferEdit(),
                    character,
                    null
                )
            )
        }

        val prepareFood = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.PRIMARY,
            "Preparar Comida",
            {
                emoji = Emoji.fromUnicode("\uD83D\uDC68\u200D\uD83C\uDF73")
            }
        ) {
            command.switchScreen(
                PrepareFoodScreen(
                    command,
                    user,
                    it.deferEdit(),
                    character,
                    listOf()
                )
            )
        }

        val refresh = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.SECONDARY,
            "Atualizar"
        ) {
            this.hook = it.deferEdit()
            command.switchScreen(this)
        }

        val closeSession = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.DANGER,
            "Encerrar Sessão"
        ) { context ->
            context.reply(true) {
                styled(
                    "Encerra a sua sessão do LoriTuber, as suas necessidades ficarão congeladas até você voltar ao jogo. Você pode ficar offline por, no máximo 24 horas."
                )
                styled(
                    "Você ainda irá ganhar dinheiro com os seus vídeos."
                )
            }
        }

        val goToComputerPartsShop = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.PRIMARY,
            "Ir para a SparkBytes",
            {
                emoji = Emoji.fromUnicode("⚡")
            }
        ) {
            command.switchScreen(
                ComputerShopScreen(
                    command,
                    user,
                    it.deferEdit(),
                    character
                )
            )
        }

        val goToGroceryStore = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.PRIMARY,
            "Ir para a Mercearia",
            {
                emoji = Emoji.fromUnicode("\uD83D\uDED2")
            }
        ) {
            command.switchScreen(
                GroceryStoreScreen(
                    command,
                    user,
                    it.deferEdit(),
                    character
                )
            )
        }

        val debugMenu = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.SECONDARY,
            "Menu de Depuração",
        ) {
            command.switchScreen(
                DebugMenuScreen(
                    command,
                    user,
                    it.deferEdit(),
                    character
                )
            )
        }

        val resetMotives = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.SECONDARY,
            "Resetar Necessidades",
        ) {
            this.hook = it.deferEdit()

            sendLoriTuberRPCRequestNew<SetCharacterMotivesResponse>(
                SetCharacterMotivesRequest(
                    character.id,
                    100.0,
                    100.0,
                    100.0,
                    100.0,
                    100.0,
                    100.0
                )
            )

            command.switchScreen(this)
        }

        /* val createViewers10k = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.SECONDARY,
            "Criar Viewers (10k)",
        ) {
            it.deferChannelMessage(true)

            fun bitSetToLong(bitSet: BitSet): Long {
                var value: Long = 0
                for (i in 0 until Long.SIZE_BITS) {
                    if (bitSet.get(i)) {
                        value = value or (1L shl i)
                    }
                }
                return value
            }

            val result = loritta.transaction {
                repeat(10_000) {
                    val likedCategories = LoriTuberVideoContentCategory.entries.shuffled().take(4)

                    fun generateRandomVibes(): BitSet {
                        val vibes = BitSet(LoriTuberVideoContentVibes.entries.size)
                        for (vibe in LoriTuberVideoContentVibes.entries) {
                            vibes.set(vibe.ordinal, Random().nextBoolean())
                        }
                        return vibes
                    }

                    val viewerId = LoriTuberViewers.insertAndGetId {
                        it[LoriTuberViewers.handle] = UUID.randomUUID().toString().replace("-", "")
                        // TODO: Activity ticks shouldn't be generated like this!
                        it[LoriTuberViewers.activityStartTicks] = Random().nextLong(0, 720)
                        it[LoriTuberViewers.activityEndTicks] = Random().nextLong(720, 1_441)

                        // This is a hack!
                        if (likedCategories.any { it.ordinal == 0 }) {
                            it[LoriTuberViewers.vibesCategory1] = bitSetToLong(generateRandomVibes())
                        }
                        if (likedCategories.any { it.ordinal == 1 }) {
                            it[LoriTuberViewers.vibesCategory2] = bitSetToLong(generateRandomVibes())
                        }
                        if (likedCategories.any { it.ordinal == 2 }) {
                            it[LoriTuberViewers.vibesCategory3] = bitSetToLong(generateRandomVibes())
                        }
                        if (likedCategories.any { it.ordinal == 3 }) {
                            it[LoriTuberViewers.vibesCategory4] = bitSetToLong(generateRandomVibes())
                        }
                        if (likedCategories.any { it.ordinal == 4 }) {
                            it[LoriTuberViewers.vibesCategory5] = bitSetToLong(generateRandomVibes())
                        }
                        if (likedCategories.any { it.ordinal == 5 }) {
                            it[LoriTuberViewers.vibesCategory6] = bitSetToLong(generateRandomVibes())
                        }
                        if (likedCategories.any { it.ordinal == 6 }) {
                            it[LoriTuberViewers.vibesCategory7] = bitSetToLong(generateRandomVibes())
                        }
                        if (likedCategories.any { it.ordinal == 7 }) {
                            it[LoriTuberViewers.vibesCategory8] = bitSetToLong(generateRandomVibes())
                        }
                        if (likedCategories.any { it.ordinal == 8 }) {
                            it[LoriTuberViewers.vibesCategory9] = bitSetToLong(generateRandomVibes())
                        }
                        if (likedCategories.any { it.ordinal == 9 }) {
                            it[LoriTuberViewers.vibesCategory10] = bitSetToLong(generateRandomVibes())
                        }

                    }

                    val categoriesThatILike = LoriTuberVideoContentCategory.entries
                        .shuffled()
                        .take(4)

                    for (category in categoriesThatILike) {
                        val categoryPreference = Random().nextBoolean()

                        LoriTuberViewerVideoPreferences.insert {
                            it[LoriTuberViewerVideoPreferences.viewer] = viewerId
                            it[LoriTuberViewerVideoPreferences.category] = category

                            // TODO: Instead of doing like this, we would choose 2 categories that the user really likes, then 3 neutral categories, and the rest the viewer doesn't like
                            it[LoriTuberViewerVideoPreferences.categoryPreference] = if (categoryPreference)
                                1
                            else
                                -1

                            val vibes1 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes2 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes3 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes4 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes5 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes6 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes7 = if (Random().nextBoolean())
                                1
                            else
                                -1

                            it[LoriTuberViewerVideoPreferences.vibe1Preference] = vibes1
                            it[LoriTuberViewerVideoPreferences.vibe2Preference] = vibes2
                            it[LoriTuberViewerVideoPreferences.vibe3Preference] = vibes3
                            it[LoriTuberViewerVideoPreferences.vibe4Preference] = vibes4
                            it[LoriTuberViewerVideoPreferences.vibe5Preference] = vibes5
                            it[LoriTuberViewerVideoPreferences.vibe6Preference] = vibes6
                            it[LoriTuberViewerVideoPreferences.vibe7Preference] = vibes7
                        }
                    }
                }
            }

            it.reply(true) {
                styled(
                    "Viewers criados"
                )
            }
            return@buttonForUser
        }

        val createViewers = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.SECONDARY,
            "Criar Viewers (100)",
        ) {
            it.deferChannelMessage(true)

            val result = loritta.transaction {
                repeat(100) {
                    val viewerId = LoriTuberViewers.insertAndGetId {
                        it[LoriTuberViewers.handle] = UUID.randomUUID().toString().replace("-", "")
                        // TODO: Activity ticks shouldn't be generated like this!
                        it[LoriTuberViewers.activityStartTicks] = Random().nextLong(0, 720)
                        it[LoriTuberViewers.activityEndTicks] = Random().nextLong(720, 1_441)
                    }

                    for (category in LoriTuberVideoContentCategory.entries) {
                        LoriTuberViewerVideoPreferences.insert {
                            it[LoriTuberViewerVideoPreferences.viewer] = viewerId
                            it[LoriTuberViewerVideoPreferences.category] = category

                            // TODO: Instead of doing like this, we would choose 2 categories that the user really likes, then 3 neutral categories, and the rest the viewer doesn't like
                            val categoryPreference = Random().nextBoolean()
                            it[LoriTuberViewerVideoPreferences.categoryPreference] = if (categoryPreference)
                                1
                            else
                                -1

                            val vibes1 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes2 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes3 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes4 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes5 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes6 = if (Random().nextBoolean())
                                1
                            else
                                -1
                            val vibes7 = if (Random().nextBoolean())
                                1
                            else
                                -1

                            it[LoriTuberViewerVideoPreferences.vibe1Preference] = vibes1
                            it[LoriTuberViewerVideoPreferences.vibe2Preference] = vibes2
                            it[LoriTuberViewerVideoPreferences.vibe3Preference] = vibes3
                            it[LoriTuberViewerVideoPreferences.vibe4Preference] = vibes4
                            it[LoriTuberViewerVideoPreferences.vibe5Preference] = vibes5
                            it[LoriTuberViewerVideoPreferences.vibe6Preference] = vibes6
                            it[LoriTuberViewerVideoPreferences.vibe7Preference] = vibes7
                        }
                    }
                }
            }

            it.reply(true) {
                styled(
                    "Viewers criados"
                )
            }
            return@buttonForUser
        } */

        val worldTime = WorldTime(characterStatus.currentTick)
        val worldHours = worldTime.hours
        val worldMinutes = worldTime.minutes

        hook.editOriginal(
            MessageEdit {
                embed {
                    title = characterStatus.name

                    val currentTask = when (val task = characterStatus.currentTask) {
                        is net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask.Sleeping -> "Dormindo"
                        is net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask.WorkingOnVideo -> "Trabalhando em um Vídeo"
                        is net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask.Eating -> {
                            val item = LoriTuberItems.getById(task.itemId)
                            val foodAttributes = item.foodAttributes!!

                            "Comendo ${LoriTuberItems.getById(task.itemId).id} (${foodAttributes.ticks - (characterStatus.currentTick - task.startedEatingAtTick)} ticks)"
                        }
                        is net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask.PreparingFood -> {
                            val recipe = task.recipeId?.let { LoriTuberRecipes.getById(it) }
                            val targetItem = recipe?.targetItemId?.let { LoriTuberItems.getById(it) } ?: LoriTuberItems.SLOP

                            val ticks = recipe?.ticks ?: 20 // Slop

                            // TODO: If you never made that recipe, show it as ??? (also show that for slop)
                            "Preparando Comida ${targetItem.id} (${ticks - (characterStatus.currentTick - task.startedPreparingAtTick)} ticks)"
                        }
                        null -> "Nada"
                    }

                    description = buildString {
                        appendLine("⭐ **Tarefa atual:** $currentTask")
                        // appendLine("\uD83D\uDCFB *Starry Shores Rádio*")
                        appendLine("\uD83D\uDCFB *Árvore Presa em Gato; Bombeiros Perplexos*")
                        appendLine("⏰ ${worldHours.toString().padStart(2, '0')}:${worldMinutes.toString().padStart(2, '0')}")
                        appendLine("\uD83C\uDF21\uFE0F 16º C")
                        appendLine("☁ Nublado")
                    }

                    val mood = listOf(
                        characterStatus.hungerNeed,
                        characterStatus.energyNeed,
                        characterStatus.funNeed,
                        characterStatus.hygieneNeed,
                        characterStatus.bladderNeed,
                        characterStatus.socialNeed
                    ).average()

                    field("Humor", "${mood}%", false)
                    field("Fome", "${characterStatus.hungerNeed}%")
                    field("Energia", "${characterStatus.energyNeed}%")
                    field("Diversão", "${characterStatus.funNeed}%")
                    field("Higiene", "${characterStatus.hygieneNeed}%")
                    field("Banheiro", "${characterStatus.bladderNeed}%")
                    field("Social", "${characterStatus.socialNeed}%")

                    val moodColorProgress = 1f - (mood / 100f)

                    color = easeColors(LoriTuberUtils.FULLY_HAPPY, LoriTuberUtils.FULLY_SAD, moodColorProgress.toFloat()).rgb

                    image = "https://cdn.discordapp.com/attachments/739823666891849729/1285683692311806000/lorituber_yourhome.png?ex=66eb29bf&is=66e9d83f&hm=bc5caca9fb96aae04173f08abb908f5d6e515217a4d97e95cc4743624452afe6&"
                }

                actionRow(button)
                actionRow(
                    sleep,
                    eatFood,
                    /* if (characterStatus.currentTask != null) {
                        loritta.interactivityManager.buttonForUser(
                            user,
                            cancelActionButton
                        ) {
                            val result = loritta.transaction {
                                LoriTuberCharacters.update({ LoriTuberCharacters.id eq character.id }) {
                                    it[LoriTuberCharacters.currentTask] = null
                                }
                            }

                            this@ViewMotivesScreen.hook = it.deferEdit()
                            command.switchScreen(this@ViewMotivesScreen)
                        }
                    } else {
                        cancelActionButton.asDisabled()
                    } */
                )
                actionRow(prepareFood)
                actionRow(goToComputerPartsShop, goToGroceryStore)
                actionRow(refresh, resetMotives, debugMenu, closeSession)
            }
        ).await()
    }

    private fun easeColors(startColor: Color, endColor: Color, percent: Float): Color {
        // Ensure percent is between 0.0 and 1.0
        val clampedPercent = percent.coerceIn(0.0f, 1.0f)

        // Interpolate each color component (red, green, blue)
        val red = (startColor.red + (endColor.red - startColor.red) * clampedPercent).toInt()
        val green = (startColor.green + (endColor.green - startColor.green) * clampedPercent).toInt()
        val blue = (startColor.blue + (endColor.blue - startColor.blue) * clampedPercent).toInt()

        return Color(red, green, blue)
    }

    @Serializable
    data class GetCharacterStatusResult(
        val currentTick: Long,
        val name: String,
        val energyNeed: Double,
        val hungerNeed: Double,
        val funNeed: Double,
        val hygieneNeed: Double,
        val bladderNeed: Double,
        val socialNeed: Double,
        val currentTask: LoriTuberTask?
    )
}