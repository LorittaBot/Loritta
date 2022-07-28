package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.minecraft

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.MinecraftSkinLorittaSweatshirtRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.gabrielaimageserver.exceptions.InvalidMinecraftSkinException
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.URLUtils
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import java.util.*

class McSkinLorittaSweatshirtExecutor(loritta: LorittaCinnamon, val client: GabrielaImageServerClient, val mojang: MinecraftMojangAPI) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val skin = string("skin", MinecraftCommand.I18N_PREFIX.Sweatshirt.Options.Skin.Text)

        val sweatshirtStyle = string("sweatshirt_style", MinecraftCommand.I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Text) {
            choice(
                MinecraftCommand.I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.Light,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.LIGHT.name.lowercase()
            )
            choice(
                MinecraftCommand.I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.Dark,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.DARK.name.lowercase()
            )
            choice(
                MinecraftCommand.I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixWavy,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_WAVY.name.lowercase()
            )
            choice(
                MinecraftCommand.I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixWavyWithStitches,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_WAVY_WITH_STITCHES.name.lowercase()
            )
            choice(
                MinecraftCommand.I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixVertical,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_VERTICAL.name.lowercase()
            )
            choice(
                MinecraftCommand.I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixVerticalWithStitches,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_VERTICAL_WITH_STITCHES.name.lowercase()
            )
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val playerNameOrUrl = args[options.skin]
        val sweatshirtStyleName = args[options.sweatshirtStyle]

        val imageData = if (URLUtils.isValidHttpOrHttpsURL(playerNameOrUrl)) {
            URLImageData(playerNameOrUrl)
        } else {
            if (!playerNameOrUrl.matches(McSkinExecutor.VALID_NAME_REGEX))
                context.failEphemerally(
                    prefix = Emotes.Error,
                    content = context.i18nContext.get(MinecraftCommand.I18N_CATEGORY_PREFIX.InvalidPlayerName(playerNameOrUrl))
                )

            val profile = mojang.getUserProfileFromName(playerNameOrUrl) ?: context.failEphemerally(
                prefix = Emotes.Error,
                content = context.i18nContext.get(MinecraftCommand.I18N_CATEGORY_PREFIX.UnknownPlayer(playerNameOrUrl))
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
                        playerName = playerNameOrUrl,
                        skinType = if (isSteve) "Steve" else "Alex"
                    )
                )
            )

            URLImageData(skinUrl)
        }

        val image = try {
            client.images.minecraftSkinLorittaSweatshirt(
                MinecraftSkinLorittaSweatshirtRequest(
                    imageData,
                    MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.valueOf(sweatshirtStyleName.uppercase())
                )
            )
        } catch (e: InvalidMinecraftSkinException) {
            context.failEphemerally(
                prefix = Emotes.Error,
                content = context.i18nContext.get(
                    I18nKeysData.Commands.Command.Minecraft.Sweatshirt.InvalidMinecraftSkin
                )
            )
        }

        context.sendMessage {
            styled(
                context.i18nContext.get(
                    I18nKeysData.Commands.Command.Minecraft.Sweatshirt.Result
                ),
                Emotes.LoriHeart
            )

            addFile("skin_lori_sweatshirt.png", image.inputStream())
        }
    }

    private fun convertNonDashedToUniqueID(id: String): UUID {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32))
    }
}