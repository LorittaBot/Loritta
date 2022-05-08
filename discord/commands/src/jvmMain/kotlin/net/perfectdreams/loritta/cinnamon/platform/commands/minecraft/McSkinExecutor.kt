package net.perfectdreams.loritta.cinnamon.platform.commands.minecraft

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import java.util.*

class McSkinExecutor(val mojang: MinecraftMojangAPI) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val username = string("player_name", MinecraftCommand.I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val player = args[Options.username]
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