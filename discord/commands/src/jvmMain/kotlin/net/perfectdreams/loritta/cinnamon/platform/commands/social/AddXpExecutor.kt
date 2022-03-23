package net.perfectdreams.loritta.cinnamon.platform.commands.social

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.*
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.EditXpCommand
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId


class AddXpExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(AddXpExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val amount = integer("amount", EditXpCommand.I18N_PREFIX.Add.Options.Amount)
                .register()
            val user = optionalUser("user", EditXpCommand.I18N_PREFIX.Add.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.fail {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        val user = args[Options.user] ?: context.user
        val amount = args[Options.amount]

        if (amount <= 0)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(EditXpCommand.I18N_PREFIX.InvalidRange),
                    Emotes.Error
                )
            }

        val userProfile = context.loritta.services.servers.getOrCreateGuildUserProfile(
            UserId(user.id.value),
            context.guildId.value
        )

        context.loritta.services.servers.addXp(userProfile, amount)

        context.sendMessage {
            styled(
                context.i18nContext.get(EditXpCommand.I18N_PREFIX.EditedSuccessfully("<@${user.id.value}>")),
                Emotes.Tada
            )
        }
    }
}