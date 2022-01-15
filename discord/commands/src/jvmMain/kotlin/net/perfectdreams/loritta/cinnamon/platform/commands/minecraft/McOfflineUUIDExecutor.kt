package net.perfectdreams.loritta.cinnamon.platform.commands.minecraft

import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import java.util.*

class McOfflineUUIDExecutor() : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(McOfflineUUIDExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val username = string("player_name", MinecraftCommand.I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val player = args[Options.username]

        val uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:$player").toByteArray(Charsets.UTF_8))

        context.sendReply(
            context.i18nContext.get(
                MinecraftCommand.I18N_PREFIX.Player.Offlineuuid.Result(
                    player,
                    uuid.toString()
                )
            )
        )
    }
}