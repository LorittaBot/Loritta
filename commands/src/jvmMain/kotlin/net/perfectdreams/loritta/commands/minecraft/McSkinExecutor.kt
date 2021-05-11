package net.perfectdreams.loritta.commands.minecraft

import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

class McSkinExecutor(val emotes: Emotes, val mojang: MinecraftMojangAPI) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(McSkinExecutor::class) {
        object Options : CommandOptions() {
            val username = string("player_name", LocaleKeyData("commands.category.minecraft.playerNameJavaEdition"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val player = args[Options.username]
        val profile = mojang.getUserProfileFromName(player) ?: context.fail(
            prefix = emotes.error,
            content = context.locale["commands.category.minecraft.unknownPlayer", player]
        ) { isEphemeral = true }

        // TODO: Localization
        val skinUrl = profile.textures["SKIN"]?.url ?: context.fail(
            prefix = emotes.error,
            content = "Player não possui skin!"
        ) { isEphemeral = true }

        context.sendMessage(skinUrl)
    }
}