package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.minecraft

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import java.util.*

class McOfflineUUIDExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val username = string("player_name", MinecraftCommand.I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val player = args[options.username]

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