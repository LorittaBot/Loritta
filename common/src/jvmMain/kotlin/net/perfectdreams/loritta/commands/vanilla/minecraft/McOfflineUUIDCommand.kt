package net.perfectdreams.loritta.commands.vanilla.minecraft

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.commands.vanilla.minecraft.declarations.MinecraftCommandDeclaration
import java.nio.charset.StandardCharsets
import java.util.*

class McOfflineUUIDCommand(val m: LorittaBot) : LorittaCommand<CommandContext>(MinecraftCommandDeclaration.OfflineUUID, MinecraftCommandDeclaration.Root) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.mcofflineuuid"
    }

    override suspend fun executes(context: CommandContext) {
        val player = context.optionsManager.getString(MinecraftCommandDeclaration.PlayerNameOptions.playerName)
        val uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:$player").toByteArray(StandardCharsets.UTF_8))
        // TODO: Fix
        context.sendMessage(context.getUserMention(true) + context.locale["commands.command.mcofflineuuid.result", player, uuid.toString()])
    }
}