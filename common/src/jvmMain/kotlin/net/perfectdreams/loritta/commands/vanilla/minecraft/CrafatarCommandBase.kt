package net.perfectdreams.loritta.commands.vanilla.minecraft

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.minecraft.declarations.MinecraftCommandDeclaration
import net.perfectdreams.loritta.utils.minecraft.MCUtils

open class CrafatarCommandBase(
    val m: LorittaBot,
    val type: String,
    declaration: CommandDeclaration,
) : LorittaCommand<CommandContext>(declaration, MinecraftCommandDeclaration.Root) {
    override suspend fun executes(context: CommandContext) {
        val player = context.optionsManager.getString(MinecraftCommandDeclaration.PlayerNameOptions.playerName)
        val uuid = MCUtils.getUniqueId(player)

        if (uuid == null) {
            // TODO: Fix
            context.reply(
                LorittaReply(
                    context.locale["commands.category.minecraft.unknownPlayer", player],
                    // Constants.ERROR
                )
            )
            return
        }

        context.sendMessage("https://crafatar.com/$type/$uuid?size=128&overlay")
    }
}