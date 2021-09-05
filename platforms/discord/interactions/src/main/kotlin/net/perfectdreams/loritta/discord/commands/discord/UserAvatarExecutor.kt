package net.perfectdreams.loritta.discord.commands.discord

import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.discord.commands.discord.declarations.UserCommand

class UserAvatarExecutor(val emotes: Emotes, val lorittaId: Long) : CommandExecutor() {
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
                    user.id == 390927821997998081L -> UserCommand.I18N_PREFIX.Avatar.PantufaEasterEgg

                    // Easter Egg: Gabriela
                    user.id == 481901252007952385L -> UserCommand.I18N_PREFIX.Avatar.GabrielaEasterEgg

                    // Easter Egg: Carl-bot
                    user.id == 235148962103951360L -> UserCommand.I18N_PREFIX.Avatar.CarlbotEasterEgg

                    // Easter Egg: Dank Memer
                    user.id == 270904126974590976L -> UserCommand.I18N_PREFIX.Avatar.DankMemerEasterEgg

                    // Easter Egg: Mantaro
                    user.id == 213466096718708737L -> UserCommand.I18N_PREFIX.Avatar.MantaroEasterEgg

                    // Easter Egg: Erisly
                    user.id == 169678500893163520L -> UserCommand.I18N_PREFIX.Avatar.ErislyEasterEgg

                    // Easter Egg: Kuraminha
                    user.id == 640593174171090984L -> UserCommand.I18N_PREFIX.Avatar.KuraminhaEasterEgg

                    // Nothing else, just use null
                    else -> null
                }

                // If the text is present, set it as the footer!
                if (easterEggFooterTextKey != null)
                    footer(context.i18nContext.get(easterEggFooterTextKey))

                color(114, 137, 218) // TODO: Move this to an object
                image("${user.avatar.url}?size=2048")
            }
        }
    }
}