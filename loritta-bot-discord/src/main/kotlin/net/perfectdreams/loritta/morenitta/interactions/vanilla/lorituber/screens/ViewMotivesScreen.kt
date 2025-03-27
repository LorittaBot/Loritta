package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask
import net.perfectdreams.loritta.serializable.lorituber.requests.CancelTaskRequest
import net.perfectdreams.loritta.serializable.lorituber.requests.GetChannelsByCharacterRequest
import net.perfectdreams.loritta.serializable.lorituber.requests.GetCharacterStatusRequest
import net.perfectdreams.loritta.serializable.lorituber.requests.StartTaskRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.CancelTaskResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.GetChannelsByCharacterResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.GetCharacterStatusResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.StartTaskResponse

class ViewMotivesScreen(command: LoriTuberCommand, user: User, hook: InteractionHook, val character: LoriTuberCommand.PlayerCharacter) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        if (command.checkMail(user, hook, character, this))
            return

        val characterStatus = sendLoriTuberRPCRequest<GetCharacterStatusResponse>(GetCharacterStatusRequest(character.id))

        when (characterStatus.currentTask) {
            is LoriTuberTask.Sleeping -> {
                val button = loritta.interactivityManager.buttonForUser(
                    user,
                    false,
                    ButtonStyle.PRIMARY,
                    "Acordar"
                ) {
                    this.hook = it.deferEdit()
                    sendLoriTuberRPCRequest<CancelTaskResponse>(CancelTaskRequest(character.id))
                    command.switchScreen(this)
                }

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = characterStatus.name

                            description = "Mimindo"
                        }

                        actionRow(button)
                    }
                ).await()
            }
            is LoriTuberTask.WorkingOnVideo -> {
                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = characterStatus.name

                            description = "Trabalhando em um vídeo"
                        }
                    }
                ).setReplace(true).await()
            }
            null -> {
                val button = loritta.interactivityManager.buttonForUser(
                    user,
                    false,
                    ButtonStyle.PRIMARY,
                    "Ver Canal no LoriTube",
                    {
                        emoji = Emoji.fromUnicode("\uD83C\uDF9E️")
                    }
                ) {
                    val channel = sendLoriTuberRPCRequest<GetChannelsByCharacterResponse>(GetChannelsByCharacterRequest(character.id))
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

                val sleep = loritta.interactivityManager.buttonForUser(
                    user,
                    false,
                    ButtonStyle.PRIMARY,
                    "Dormir",
                    {
                        emoji = Emoji.fromUnicode("\uD83D\uDE34")
                    }
                ) {
                    when (sendLoriTuberRPCRequest<StartTaskResponse>(StartTaskRequest(character.id, LoriTuberTask.Sleeping()))) {
                        is StartTaskResponse.Success -> {
                            this.hook = it.deferEdit()
                            command.switchScreen(this)
                        }
                        is StartTaskResponse.CharacterIsAlreadyDoingAnotherTask -> it.deferEdit().editOriginal(
                            MessageEdit {
                                content = "Você já está fazendo outra tarefa!"
                            }
                        ).setReplace(true).await()
                    }
                    // createUI(user, it.deferEdit(), LoriTuberCommand.LoriTuberScreen.Sleeping(screen.character))
                }

                val goToComputerPartsShop = loritta.interactivityManager.buttonForUser(
                    user,
                    false,
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

                val debugMenu = loritta.interactivityManager.buttonForUser(
                    user,
                    false,
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

                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = characterStatus.name

                            description = "\uD83D\uDCFB *Starry Shores Rádio*\n⏰ 19:00\n\uD83C\uDF21️ 16º C\n☁ Nublado"

                            field("Fome", "${characterStatus.hunger}%")
                            field("Energia", "${characterStatus.energy}%")
                        }

                        actionRow(button)
                        actionRow(sleep)
                        actionRow(goToComputerPartsShop)
                        actionRow(debugMenu)
                    }
                ).await()
            }
        }
    }
}