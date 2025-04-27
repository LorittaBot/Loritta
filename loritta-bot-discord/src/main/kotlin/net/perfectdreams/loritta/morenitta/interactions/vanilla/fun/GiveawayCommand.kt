package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class GiveawayCommand(val m: LorittaBot) : SlashCommandDeclarationWrapper  {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Giveaway
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN, UUID.fromString("e934638a-3297-4d93-8c7f-82941825e57c")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)
        this.enableLegacyMessageSupport = true
        this.alternativeLegacyLabels.apply {
            this.add("sorteio")
        }

        subcommand(I18N_PREFIX.Setup.Label, I18N_PREFIX.Setup.Description, UUID.fromString("ad7ffd12-3817-44a1-930a-4dcd7351c034")) {
            this.executor = GiveawaySetupExecutor(m)
            this.alternativeLegacyLabels.apply {
                this.add("criar")
                this.add("create")
            }
        }
    }

    class GiveawaySetupExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val builder = GiveawayBuilderScreen.GiveawayBuilder(
                context.i18nContext.get(I18N_PREFIX.Setup.GiveawayNamePlaceholder),
                context.i18nContext.get(I18N_PREFIX.Setup.GiveawayDescriptionPlaceholder)
            )

            val screen = GiveawayBuilderScreen.Appearance(m)

            context.reply(false, screen.render(context, builder))
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return emptyMap()
        }
    }
}