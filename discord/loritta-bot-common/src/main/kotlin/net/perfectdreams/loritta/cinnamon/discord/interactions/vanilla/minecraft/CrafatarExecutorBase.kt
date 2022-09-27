package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.minecraft

import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI

open class CrafatarExecutorBase(
    loritta: LorittaBot,
    val type: String,
    val mojang: MinecraftMojangAPI
) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val username = string("player_name", MinecraftCommand.I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val player = args[options.username]
        if (!player.matches(McSkinExecutor.VALID_NAME_REGEX))
            context.failEphemerally(
                prefix = Emotes.Error,
                content = context.i18nContext.get(MinecraftCommand.I18N_CATEGORY_PREFIX.InvalidPlayerName(player))
            )

        val uuid = mojang.getUniqueId(player) ?: context.failEphemerally(
            prefix = Emotes.Error,
            content = context.i18nContext.get(MinecraftCommand.I18N_CATEGORY_PREFIX.UnknownPlayer(player))
        )

        context.sendMessage("https://crafatar.com/$type/$uuid?size=128&overlay")
    }
}