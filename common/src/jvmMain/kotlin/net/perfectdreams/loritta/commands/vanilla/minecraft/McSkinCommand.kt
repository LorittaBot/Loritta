package net.perfectdreams.loritta.commands.vanilla.minecraft

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.minecraft.declarations.MinecraftCommandDeclaration
import net.perfectdreams.loritta.utils.minecraft.MCUtils

class McSkinCommand(val m: LorittaBot) : LorittaCommand<CommandContext>(MinecraftCommandDeclaration.Skin, MinecraftCommandDeclaration.Root) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.mcskin"
    }

    override suspend fun executes(context: CommandContext) {
        val player = context.optionsManager.getString(MinecraftCommandDeclaration.PlayerNameOptions.playerName)
        val profile = MCUtils.getUserProfileFromName(player)

        if (profile == null) {
            // TODO: Fix
            context.reply(
                LorittaReply(
                    context.locale["commands.category.minecraft.unknownPlayer", player],
                    // Constants.ERROR
                )
            )
            return
        }

        val skinUrl = profile.textures["SKIN"]?.url
        if (skinUrl == null) {
            // TODO: Fix and Localize
            context.reply(
                LorittaReply(
                    "Player não possui skin!",
                    // Constants.ERROR
                )
            )
            return
        }

        context.sendMessage(skinUrl)
    }
}