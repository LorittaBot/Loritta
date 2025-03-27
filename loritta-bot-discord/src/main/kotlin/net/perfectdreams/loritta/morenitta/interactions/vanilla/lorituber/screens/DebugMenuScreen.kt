package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.lorituber.requests.GetServerInfoRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.GetServerInfoResponse
import kotlin.time.Duration.Companion.milliseconds

class DebugMenuScreen(command: LoriTuberCommand, user: User, hook: InteractionHook, val character: LoriTuberCommand.PlayerCharacter) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        val response = sendLoriTuberRPCRequest<GetServerInfoResponse>(GetServerInfoRequest())

        val viewMotivesButton = loritta.interactivityManager.buttonForUser(
            user,
            false,
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
                    title = "Menu de Depuração"

                    description = "Menu de Depuração do Servidor do LoriTuber\n\nO servidor do LoriTuber roda a 4 ticks por segundo, ou seja, 250ms a cada tick.\n\nA duração média de um tick precisa ser menor que 250ms, caso seja maior, significa que o jogo está sobrecarregando e está rodando mais lento do que deveria!"

                    field("Server Tick", response.currentTick.toString())
                    field("Duração Média de um Tick", response.averageTickDuration.milliseconds.toString())
                    field("Última Atualização", "<t:${response.lastUpdate / 1_000}:R>")
                }

                actionRow(
                    viewMotivesButton
                )
            }
        ).setReplace(true).await()
    }
}