package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.perfectdreams.loritta.lorituber.rpc.packets.CreateChannelRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.CreateChannelResponse
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await

class CreateChannelScreen(command: LoriTuberCommand, user: User, hook: InteractionHook, val character: LoriTuberCommand.PlayerCharacter) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        val createChannelButton = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.PRIMARY,
            "Criar Canal"
        ) {
            val characterNameOption = modalString("Nome do Canal", TextInputStyle.SHORT)

            it.sendModal(
                "Criação de Canal",
                listOf(ActionRow.of(characterNameOption.toJDA()))
            ) { it, args ->
                val channelName = args[characterNameOption]

                val result = sendLoriTuberRPCRequestNew<CreateChannelResponse>(CreateChannelRequest(character.id, channelName))

                /* val result = loritta.transaction {
                    val canCreateANewChannel = LoriTuberChannels.select {
                        LoriTuberChannels.owner eq character.id
                    }.count() == 0L

                    if (!canCreateANewChannel)
                        return@transaction CreateChannelResult.CharacterAlreadyHasTooManyChannels
                    else {
                        val newChannel = LoriTuberChannels.insert {
                            it[LoriTuberChannels.owner] = character.id
                            it[LoriTuberChannels.name] = channelName
                        }

                        // TODO: If the user already has an channel, show a different message, maybe something like "Player is so good, that they created a second channel!"
                        LoriTuberMails.insert {
                            it[LoriTuberMails.character] = this@CreateChannelScreen.character.id
                            it[LoriTuberMails.date] = Instant.now()
                            it[LoriTuberMails.type] = Json.encodeToString<LoriTuberMail>(LoriTuberMail.BeginnerChannelCreated(this@CreateChannelScreen.character.id, newChannel[LoriTuberChannels.id].value))
                            it[LoriTuberMails.acknowledged] = false
                        }

                        return@transaction CreateChannelResult.Success(newChannel[LoriTuberChannels.id].value, newChannel[LoriTuberChannels.name])
                    }
                } */

                when (result) {
                    is CreateChannelResponse.Success -> command.switchScreen(
                        ViewChannelScreen(
                            command,
                            user,
                            it.deferEdit().jdaHook,
                            character,
                            result.id
                        )
                    )
                    is CreateChannelResponse.CharacterAlreadyHasTooManyChannels -> it.deferEdit().jdaHook.editOriginal(
                        MessageEdit {
                            content = "Você já tem muitos canais no LoriTube!"
                        }
                    ).setReplace(true).await()
                }
            }
        }

        hook.editOriginal(
            MessageEdit {
                embed {
                    description = """criar canal owo
                        
                        yay vida de digital influencer ltda
                    """.trimIndent()

                    image = "https://cdn.discordapp.com/attachments/739823666891849729/1052320130002059274/starry_shores_16x9.png"
                }

                actionRow(createChannelButton)
            }
        ).await()
    }

    sealed class CreateChannelResult {
        data object CharacterAlreadyHasTooManyChannels : CreateChannelResult()
        data class Success(val channelId: Long, val name: String) : CreateChannelResult()
    }
}