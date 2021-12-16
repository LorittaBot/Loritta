package net.perfectdreams.loritta.cinnamon.platform.commands.social

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.EditXpCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class SetXpExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(SetXpExecutor::class) {
        object Options : CommandOptions() {
            val amount = integer("amount", EditXpCommand.I18N_PREFIX.Set.Options.Amount)
                .register()
            val user = optionalUser("user", EditXpCommand.I18N_PREFIX.Set.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.fail {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        val amount = args[Options.amount]
        val user = args[Options.user] ?: context.user

        if (amount > 0)
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

        context.loritta.services.servers.setXp(userProfile, amount)

        context.sendMessage {
            styled(
                context.i18nContext.get(EditXpCommand.I18N_PREFIX.EditedSuccessfully("<@${user.id.value}>")),
                Emotes.Tada
            )
        }
    }
}