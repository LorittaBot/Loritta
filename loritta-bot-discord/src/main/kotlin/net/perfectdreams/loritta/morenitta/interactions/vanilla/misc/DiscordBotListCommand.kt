package net.perfectdreams.loritta.morenitta.interactions.vanilla.misc

import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.utils.Constants
import java.util.UUID

class DiscordBotListCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Dbl
    }

    override fun command() = slashCommand(
        I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MISC,
        uniqueId = UUID.fromString("f91c8c3e-31b3-4e7c-b17a-d5e9a9c8432c")
    ) {
        alternativeLegacyLabels.apply {
            add("upvote")
        }
        enableLegacyMessageSupport = true

        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        executor = DiscordBotListExecutor()
    }

    inner class DiscordBotListExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(false) {
                embed {
                    title = "✨ Discord Bot List"
                    color = Constants.LORITTA_AQUA.rgb
                    thumbnail = "${loritta.config.loritta.website.url}assets/img/loritta_star.png"
                    description = context.i18nContext.get(
                        I18N_PREFIX.Info(
                            Emotes.DISCORD_BOT_LIST,
                            context.config.commandPrefix,
                            "https://top.gg/bot/${loritta.config.loritta.discord.applicationId}"
                        )
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }
}