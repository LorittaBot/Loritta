package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.minecraft

import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import java.util.*

class McSkinExecutor(loritta: LorittaBot, val mojang: MinecraftMojangAPI) : CinnamonSlashCommandExecutor(loritta) {
    companion object {
        val VALID_NAME_REGEX = Regex("[a-zA-Z0-9_]{2,16}")
    }

    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val username = string("player_name", MinecraftCommand.I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val player = args[options.username]
        if (!player.matches(VALID_NAME_REGEX))
            context.failEphemerally(
                prefix = Emotes.Error,
                content = context.i18nContext.get(MinecraftCommand.I18N_CATEGORY_PREFIX.InvalidPlayerName(player))
            )

        val profile = mojang.getUserProfileFromName(player) ?: context.failEphemerally(
            prefix = Emotes.Error,
            content = context.i18nContext.get(MinecraftCommand.I18N_CATEGORY_PREFIX.UnknownPlayer(player))
        )

        // Whether the player has the “Alex?” or “Steve?” skin depends on the Java hashCode of their UUID. Steve is used for even hashes. Example implementations:
        // https://wiki.vg/Mojang_API
        // TODO: This should be migrated to MinecraftMojangAPI instead of using hacky workarounds here
        val uniqueId = convertNonDashedToUniqueID(profile.profileId)
        val isSteve = uniqueId.hashCode() % 2 == 1

        val skinUrl = profile.textures["SKIN"]?.url ?: context.failEphemerally(
            prefix = Emotes.Error,
            content = context.i18nContext.get(
                I18nKeysData.Commands.Command.Minecraft.Player.Skin.PlayerDoesNotHaveASkin(
                    playerName = player,
                    skinType = if (isSteve) "Steve" else "Alex"
                )
            )
        )

        context.sendMessage(skinUrl)
    }

    private fun convertNonDashedToUniqueID(id: String): UUID {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32))
    }
}