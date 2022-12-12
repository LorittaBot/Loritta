package net.perfectdreams.loritta.morenitta.interactions.vanilla.dreamland

import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString

class DreamLandCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Dreamland
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY) {
        executor = DreamLandExecutor()
    }

    inner class DreamLandExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.deferChannelMessage()

            context.reply(false) {
                content = "Bem-vindx a DreamLand!"

                actionRow(
                    // TODO: Restrict button to an specific user
                    loritta.interactivityManager.button(
                        ButtonStyle.PRIMARY,
                        "Criar Personagem"
                    ) {
                        val characterNameOption = modalString("Nome do Personagem", TextInputStyle.SHORT)

                        it.sendModal(
                            "Criação de Personagem",
                            listOf(ActionRow.of(characterNameOption.toJDA()))
                        ) { it, args ->
                            val result = args[characterNameOption]

                            it.reply(false) {
                                content = "Nome: $result"
                            }
                        }
                    }
                )
            }
        }
    }
}