package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberCharacters
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberServerInfos
import net.perfectdreams.loritta.lorituber.LoriTuberServer
import net.perfectdreams.loritta.lorituber.ServerInfo
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.selectAll
import kotlin.time.Duration.Companion.milliseconds

class DebugMenuScreen(command: LoriTuberCommand, user: User, hook: InteractionHook, val character: LoriTuberCommand.PlayerCharacter) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        val (serverInfo, totalCharacters) = loritta.transaction {
            val serverInfo = LoriTuberServerInfos.selectAll()
                .where { LoriTuberServerInfos.type eq LoriTuberServer.GENERAL_INFO_KEY }
                .first()
                .get(LoriTuberServerInfos.data)
                .let { Json.decodeFromString<ServerInfo>(it) }

            val totalCharacters = LoriTuberCharacters.selectAll().count()

            Pair(serverInfo, totalCharacters)
        }

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

        hook.editOriginal(
            MessageEdit {
                embed {
                    title = "Menu de Depuração"

                    // description = "Menu de Depuração do Servidor do LoriTuber\n\nO servidor do LoriTuber roda a 4 ticks por segundo, ou seja, 250ms a cada tick.\n\nA duração média de um tick precisa ser menor que 250ms, caso seja maior, significa que o jogo está sobrecarregando e está rodando mais lento do que deveria!"
                    description = "Menu de Depuração do Servidor do LoriTuber\n\nO servidor do LoriTuber roda a ${LoriTuberServer.TICKS_PER_SECOND} ticks por segundo, ou seja, ${LoriTuberServer.TICK_DELAY}ms a cada tick.\n\nA duração média de um tick precisa ser menor que ${LoriTuberServer.TICK_DELAY}ms, caso seja maior, significa que o jogo está sobrecarregando e está rodando mais lento do que deveria!"

                    val averageTickDuration = serverInfo.averageTickDuration?.milliseconds?.toString() ?: "???"
                    field("Server Tick", serverInfo.currentTick.toString())
                    field("Duração Média de um Tick", averageTickDuration)
                    field("Última Atualização", "<t:${serverInfo.lastUpdate / 1_000}:R>")
                    field("Quantidade de Influencers no Lorituber", "$totalCharacters")
                    // TODO: Show how many viewers are available
                }

                actionRow(
                    viewMotivesButton
                )
            }
        ).setReplace(true).await()
    }
}