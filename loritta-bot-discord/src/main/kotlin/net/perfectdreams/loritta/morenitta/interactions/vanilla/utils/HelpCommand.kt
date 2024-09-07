package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import dev.kord.common.Color
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class HelpCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Help
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.UTILS, UUID.fromString("22fb7d69-cabd-4446-9f91-213c5436dcfe")) {
        enableLegacyMessageSupport = true

        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        executor = HelpExecutor()
    }

    inner class HelpExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(false) {
                embed {
                    title = "${Emotes.LoriHeart} ${context.i18nContext.get(I18N_PREFIX.LorittaHelp)}"
                    description = context.i18nContext.get(I18N_PREFIX.Intro(context.user.asMention)).joinToString("\n\n")

                    color = Color(26, 160, 254).rgb // TODO: Move this to a object

                    field(
                        "${Emotes.LoriPat} ${context.i18nContext.get(I18N_PREFIX.CommandList)}",
                        "${context.loritta.config.loritta.website.url}commands"
                    )
                    field(
                        "${Emotes.LoriHm} ${context.i18nContext.get(I18N_PREFIX.SupportServer)}",
                        "${context.loritta.config.loritta.website.url}support"
                    )
                    field(
                        "${Emotes.LoriYay} ${context.i18nContext.get(I18N_PREFIX.AddMe)}",
                        "${context.loritta.config.loritta.website.url}dashboard"
                    )
                    field(
                        "${Emotes.LoriRich} ${context.i18nContext.get(I18N_PREFIX.Donate)}",
                        "${context.loritta.config.loritta.website.url}donate"
                    )
                    field(
                        "${Emotes.LoriReading} ${context.i18nContext.get(I18N_PREFIX.Blog)}",
                        "${context.loritta.config.loritta.website.url}blog"
                    )
                    field(
                        "${Emotes.LoriRage} ${context.i18nContext.get(I18N_PREFIX.Guidelines)}",
                        "${context.loritta.config.loritta.website.url}guidelines"
                    )

                    thumbnail = context.loritta.config.loritta.website.url + "assets/img/lori_help_short.png"
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }
}