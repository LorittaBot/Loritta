package net.perfectdreams.loritta.commands.minecraft

import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.minecraft.MinecraftMojangAPI

class McSkinExecutor(val emotes: Emotes, val mojang: MinecraftMojangAPI) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(McSkinExecutor::class) {
        object Options : CommandOptions() {
            val username = string("player_name", LocaleKeyData("TODO_FIX_THIS"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val player = args[Options.username]
        val profile = mojang.getUserProfileFromName(player)

        if (profile == null) {
            context.sendReply {
                content = context.locale["commands.category.minecraft.unknownPlayer", player]
                prefix = emotes.error.toString()
            }
            return
        }

        val skinUrl = profile.textures["SKIN"]?.url
        if (skinUrl == null) {
            // TODO: Localization
            context.sendReply {
                content = "Player não possui skin!"
                prefix = emotes.error.toString()
            }
            return
        }

        context.sendMessage(skinUrl)
    }
}