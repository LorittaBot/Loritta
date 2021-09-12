package net.perfectdreams.loritta.cinnamon.platform.commands.minecraft

import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McUUIDExecutor(val mojang: MinecraftMojangAPI) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(McUUIDExecutor::class) {
        object Options : CommandOptions() {
            val username = string("player_name", MinecraftCommand.I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        val player = args[Options.username]

        val onlineUniqueId = mojang.getUniqueId(player) ?: context.failEphemerally(
            prefix = Emotes.Error,
            content = context.i18nContext.get(MinecraftCommand.I18N_CATEGORY_PREFIX.UnknownPlayer(player))
        )

        context.sendReply(
            context.i18nContext.get(
                MinecraftCommand.I18N_PREFIX.Player.Onlineuuid.Result(
                    player,
                    onlineUniqueId.toString()
                )
            )
        )
    }
}