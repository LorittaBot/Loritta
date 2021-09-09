package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.builder.message.create.embed
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.utils.NotableUserIds

class UserAvatarExecutor(val lorittaId: Snowflake) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(UserAvatarExecutor::class) {
        object Options : CommandOptions() {
            val user = optionalUser("user", UserCommand.I18N_PREFIX.Avatar.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val user = args[Options.user] ?: context.user

        context.sendMessage {
            embed {
                title = "\uD83D\uDDBC ${user.name}"
                description = "**${context.i18nContext.get(UserCommand.I18N_PREFIX.Avatar.ClickHere("${user.avatar.url}?size=2048"))}**"

                // Specific User Avatar Easter Egg Texts
                val easterEggFooterTextKey = when {
                    // Easter Egg: Looking up yourself
                    context.user.id == user.id -> UserCommand.I18N_PREFIX.Avatar.YourselfEasterEgg

                    // Easter Egg: Loritta/Application ID
                    // TODO: Show who made the fan art during the Fan Art Extravaganza
                    user.id == lorittaId -> UserCommand.I18N_PREFIX.Avatar.LorittaEasterEgg

                    // Easter Egg: Pantufa
                    user.id == NotableUserIds.PANTUFA -> UserCommand.I18N_PREFIX.Avatar.PantufaEasterEgg

                    // Easter Egg: Gabriela
                    user.id == NotableUserIds.GABRIELA -> UserCommand.I18N_PREFIX.Avatar.GabrielaEasterEgg

                    // Easter Egg: Carl-bot
                    user.id == NotableUserIds.CARLBOT -> UserCommand.I18N_PREFIX.Avatar.CarlbotEasterEgg

                    // Easter Egg: Dank Memer
                    user.id == NotableUserIds.DANK_MEMER -> UserCommand.I18N_PREFIX.Avatar.DankMemerEasterEgg

                    // Easter Egg: Mantaro
                    user.id == NotableUserIds.MANTARO -> UserCommand.I18N_PREFIX.Avatar.MantaroEasterEgg

                    // Easter Egg: Erisly
                    user.id == NotableUserIds.ERISLY -> UserCommand.I18N_PREFIX.Avatar.ErislyEasterEgg

                    // Easter Egg: Kuraminha
                    user.id == NotableUserIds.KURAMINHA -> UserCommand.I18N_PREFIX.Avatar.KuraminhaEasterEgg

                    // Nothing else, just use null
                    else -> null
                }

                // If the text is present, set it as the footer!
                if (easterEggFooterTextKey != null)
                    footer(context.i18nContext.get(easterEggFooterTextKey))

                color = Color(114, 137, 218) // TODO: Move this to an object
                image = user.avatar.url + "?size=2048"
            }
        }
    }
}