package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.requests.CreateChannelRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.CreateChannelResponse

class CreateChannelScreen(command: LoriTuberCommand, user: User, hook: InteractionHook, val character: LoriTuberCommand.PlayerCharacter) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        val createChannelButton = loritta.interactivityManager.buttonForUser(
            user,
            false,
            ButtonStyle.PRIMARY,
            "Criar Canal"
        ) {
            val characterNameOption = modalString("Nome do Canal", TextInputStyle.SHORT)

            it.sendModal(
                "Criação de Canal",
                listOf(ActionRow.of(characterNameOption.toJDA()))
            ) { it, args ->
                val result = args[characterNameOption]

                val channelResponse = sendLoriTuberRPCRequest<CreateChannelResponse>(
                    CreateChannelRequest(
                        character.id,
                        result
                    )
                )

                when (channelResponse) {
                    is CreateChannelResponse.Success -> command.switchScreen(
                        ViewChannelScreen(
                            command,
                            user,
                            it.deferEdit().jdaHook,
                            character,
                            channelResponse.id
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
}