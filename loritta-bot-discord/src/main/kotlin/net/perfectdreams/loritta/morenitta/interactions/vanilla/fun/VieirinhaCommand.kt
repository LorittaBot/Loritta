package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand

class VieirinhaCommand : SlashCommandDeclarationWrapper  {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Vieirinha
        val PUNCTUATIONS = listOf(
            "exclamation",
            "dot"
        )
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)

        executor = VieirinhaExecutor()
    }

    inner class VieirinhaExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val question = string("question", I18N_PREFIX.Options.Question) // Unused because we don't really care about it, heh
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(false) {
                embed {
                    author("Vieirinha", null, "http://i.imgur.com/rRtHdti.png")

                    description = context.i18nContext.get(
                        I18N_PREFIX.Responses(
                            PUNCTUATIONS.random()
                        )
                    ).random()
                }
            }
        }
    }
}