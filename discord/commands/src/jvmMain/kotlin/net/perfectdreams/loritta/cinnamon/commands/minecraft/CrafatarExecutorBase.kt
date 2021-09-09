package net.perfectdreams.loritta.cinnamon.commands.minecraft

import net.perfectdreams.loritta.cinnamon.commands.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptions
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

open class CrafatarExecutorBase(
    val type: String,
    val emotes: Emotes,
    val mojang: MinecraftMojangAPI
) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(CrafatarExecutorBase::class) {
        object Options : CommandOptions() {
            val username = string("player_name", MinecraftCommand.I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val player = args[Options.username]
        val uuid = mojang.getUniqueId(player) ?: context.failEphemerally(
            prefix = emotes.error,
            content = context.i18nContext.get(MinecraftCommand.I18N_CATEGORY_PREFIX.UnknownPlayer(player))
        )

        context.sendMessage("https://crafatar.com/$type/$uuid?size=128&overlay")
    }
}