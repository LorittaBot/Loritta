package net.perfectdreams.loritta.commands.vanilla.minecraft

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.commands.vanilla.minecraft.declarations.MinecraftCommandDeclaration
import net.perfectdreams.loritta.utils.minecraft.MCUtils

class McUUIDCommand(val m: LorittaBot) : LorittaCommand<CommandContext>(MinecraftCommandDeclaration.UUID, MinecraftCommandDeclaration.Root) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.mcuuid"
    }

    override suspend fun executes(context: CommandContext) {
        val player = context.optionsManager.getString(MinecraftCommandDeclaration.PlayerNameOptions.playerName)
        val playerUUID = MCUtils.getUniqueId(player)

        if (playerUUID != null) {
            // TODO: Fix
            context.sendMessage(context.getUserMention(true) + context.locale["commands.command.mcuuid.result", player, playerUUID])
        } else {
            // TODO: Fix
            context.sendMessage(/* Constants.ERROR + */ " **|** " + context.getUserMention(true) + context.locale["commands.command.mcuuid.invalid", player])
        }
    }
}