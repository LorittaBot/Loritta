package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import java.awt.Color

class ComputerShopScreen(command: LoriTuberCommand, user: User, hook: InteractionHook, val character: LoriTuberCommand.PlayerCharacter) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
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
                    title = "⚡ SparkBytes"

                    description = """Seja bem-vindo(a) a SparkBytes! Vendemos peças de computador e tecnologia, e nosso foco é trabalhar e ajudar os influenciadores digitais.
                        |
                        |Meu nome é Jerbs, e estou aqui para auxiliar você a encontrar os produtos certos para a sua jornada!
                    """.trimMargin()

                    image = "https://cdn.discordapp.com/attachments/739823666891849729/1052664743426523227/jerbs_store.png"

                    color = Color(255, 172, 51).rgb
                }

                actionRow(viewMotivesButton)
            }
        ).setReplace(true).await()
    }
}