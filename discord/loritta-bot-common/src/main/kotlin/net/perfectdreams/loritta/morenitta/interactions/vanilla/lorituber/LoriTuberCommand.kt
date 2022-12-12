package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber

import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString

class LoriTuberCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Lorituber
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY) {
        subcommandGroup(I18N_PREFIX.Character.Label, I18N_PREFIX.Character.Description) {
            subcommand(I18N_PREFIX.Character.Create.Label, I18N_PREFIX.Character.Create.Description) {
                executor = LoriTuberCharacterCreatorExecutor()
            }

            subcommand(I18N_PREFIX.Character.Change.Label, I18N_PREFIX.Character.Change.Description) {
                executor = LoriTuberCharacterChangerExecutor()
            }
        }

        subcommandGroup(I18N_PREFIX.Channel.Label, I18N_PREFIX.Channel.Description) {
            subcommand(I18N_PREFIX.Channel.Create.Label, I18N_PREFIX.Channel.Create.Description) {
                executor = LoriTuberChannelCreateExecutor()
            }

            subcommand(I18N_PREFIX.Channel.Manage.Label, I18N_PREFIX.Channel.Manage.Description) {
                executor = LoriTuberChannelManagerExecutor()
            }
        }
    }

    inner class LoriTuberCharacterCreatorExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.deferChannelMessage()

            context.reply(false) {
                content = "Bem-vindx a DreamLand!"

                actionRow(
                    loritta.interactivityManager.buttonForUser(
                        context.user,
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

    inner class LoriTuberCharacterChangerExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.reply(false) {
                styled("Atualmente não é possível ter mais de um personagem no LoriTuber!")
            }
        }
    }

    inner class LoriTuberChannelCreateExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.reply(false) {
                content = "É a hora de começar a grande jornada da Loritta Morenitta virar uma estrela de sucesso na DreamLand!"

                actionRow(
                    loritta.interactivityManager.buttonForUser(
                        context.user,
                        ButtonStyle.PRIMARY,
                        "Criar Canal"
                    ) {
                        val channelNameOption = modalString("Nome do Canal", TextInputStyle.SHORT)

                        it.sendModal(
                            "Criação do Canal",
                            listOf(ActionRow.of(channelNameOption.toJDA()))
                        ) { it, args ->
                            val result = args[channelNameOption]

                            it.reply(false) {
                                content = "Nome do Canal: $result"
                            }
                        }
                    }
                )
            }
        }
    }

    inner class LoriTuberChannelManagerExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.reply(false) {
                embed {
                    title = "Canal MrBeast6000"

                    description = "Olá Loritta Morenitta!\n\n\uD83D\uDCC8 1.000 views\n\uD83E\uDD29 1.000 inscritos"
                }

                actionRow(
                    loritta.interactivityManager.buttonForUser(
                        context.user,
                        ButtonStyle.PRIMARY,
                        "Criar um Vídeo"
                    ) {
                        it.reply(false) {
                            embed {
                                description = "Qual será o tema do vídeo?\n\nVocê pode ver a popularidade de cada tema em *insira comando aqui*\n\nLembre-se de manter um tema em comum para o seu canal!"
                            }

                            actionRow(
                                StringSelectMenu.create("dummy")
                                    .addOption("Gameplay", "gameplay")
                                    .addOption("Educacional", "educacional")
                                    .addOption("Política", "politica")
                                    .addOption("Vlog", "vlog")
                                    .build(),
                            )
                        }
                    },

                    /* loritta.interactivityManager.buttonForUser(
                        context.user,
                        ButtonStyle.PRIMARY,
                        "Livestream"
                    ) {
                        it.reply(false) {
                            content = "Livestreams são um ótimo jeito de conseguir fãs leais e fiéis ao seu conteúdo!\n\nEntretanto, você terá um retorno pequeno se fizer uma livestream enquanto não tem um público grande."
                        }
                    } */
                )
            }
        }
    }
}