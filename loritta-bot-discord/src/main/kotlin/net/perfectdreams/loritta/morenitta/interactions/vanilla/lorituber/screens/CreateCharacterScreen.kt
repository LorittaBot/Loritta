package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberCharacters
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberServerInfos
import net.perfectdreams.loritta.lorituber.LoriTuberServer
import net.perfectdreams.loritta.lorituber.ServerInfo
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class CreateCharacterScreen(command: LoriTuberCommand, user: User, hook: InteractionHook) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        val createCharacterButton = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.PRIMARY,
            "Criar Personagem"
        ) {
            val characterNameOption = modalString("Nome do Personagem", TextInputStyle.SHORT)

            it.sendModal(
                "Criação de Personagem",
                listOf(ActionRow.of(characterNameOption.toJDA()))
            ) { it, args ->
                val characterName = args[characterNameOption]

                val result = loritta.transaction {
                    val canCreateANewCharacter = LoriTuberCharacters.select {
                        LoriTuberCharacters.owner eq user.idLong
                    }.count() == 0L

                    return@transaction if (!canCreateANewCharacter)
                        CreateCharacterResult.UserAlreadyHasTooManyCharacters
                    else {
                        val serverInfo = loritta.transaction {
                            LoriTuberServerInfos.selectAll()
                                .where { LoriTuberServerInfos.type eq LoriTuberServer.GENERAL_INFO_KEY }
                                .first()
                                .get(LoriTuberServerInfos.data)
                                .let { Json.decodeFromString<ServerInfo>(it) }
                        }

                        val newCharacter = LoriTuberCharacters.insert {
                            it[LoriTuberCharacters.name] = characterName
                            it[LoriTuberCharacters.owner] = user.idLong
                            it[LoriTuberCharacters.energyNeed] = 100.0
                            it[LoriTuberCharacters.hungerNeed] = 100.0
                            it[LoriTuberCharacters.funNeed] = 100.0
                            it[LoriTuberCharacters.hygieneNeed] = 100.0
                            it[LoriTuberCharacters.bladderNeed] = 100.0
                            it[LoriTuberCharacters.socialNeed] = 100.0
                            it[LoriTuberCharacters.createdAtTick] = serverInfo.currentTick
                            it[LoriTuberCharacters.ticksLived] = 0
                        }

                        CreateCharacterResult.Success(
                            newCharacter[LoriTuberCharacters.id].value,
                            newCharacter[LoriTuberCharacters.name]
                        )
                    }
                }

                when (result) {
                    is CreateCharacterResult.Success -> command.switchScreen(
                        ViewMotivesScreen(
                            command,
                            user,
                            it.deferEdit().jdaHook,
                            LoriTuberCommand.PlayerCharacter(
                                result.id,
                                result.name,
                                100.0,
                                100.0,
                                100.0,
                                100.0,
                                100.0,
                                100.0
                            )
                        )
                    )
                    is CreateCharacterResult.UserAlreadyHasTooManyCharacters -> it.deferEdit().jdaHook.editOriginal(
                        MessageEdit {
                            content = "Você já tem muitos personagens vivendo na DreamLand!"
                        }
                    ).setReplace(true).await()
                }
            }
        }

        hook.editOriginal(
            MessageEdit {
                embed {
                    description = buildString {
                        append("Em meio ao oceano, ao sudeste do Brasil, a Loritta fundou o próprio país, a DreamLand! O lugar onde vivem empreendedores e sonhadores que gostam de viver a vida em alta velocidade, buscando a próxima ideia que conquistará o mundo.")
                        append("\n")
                        append("\n")
                        append("Entretanto, ter um país com apenas três habitates é chato. A Loritta já cansou de governar a Gabriela e a Pantufa... e as duas só passam o dia fofocando e jogando UNO! Algo tinha que ser feito para trazer novas pessoas, pois apenas ter um slogan cativante não está dando certo.")
                        append("\n")
                        append("\n")
                        append("Graças ao plano de expansão da DreamLand, nós temos um cafofo aconchegante para você em Starry Shores, e o melhor de tudo, de graça! A única condição é que você precisa financiar a sua passagem de avião, que custa 10 mil sonhos.")
                        append("\n")
                        append("\n")
                        append("Starry Shores é a capital da DreamLand para os influenciadores digitais. Perto da praia, com um clima agradável e com um centro ativo e vibrante, é a cidade perfeita para a sua nova vida na área de criação de conteúdo e de influenciador digital, e talvez virar uma estrela, iguais aos reflexos das estrelas no mar de Starry Shores.")
                        append("\n")
                        append("\n")
                        append("Preparado para se mudar para Starry Shores?")
                    }

                    image = "https://cdn.discordapp.com/attachments/739823666891849729/1052320130002059274/starry_shores_16x9.png"
                }

                actionRow(createCharacterButton)
            }
        ).await()
    }

    @Serializable
    sealed interface CreateCharacterResult {
        @Serializable
        data class Success(
            val id: Long,
            val name: String
        ) : CreateCharacterResult

        @Serializable
        data object UserAlreadyHasTooManyCharacters : CreateCharacterResult
    }
}