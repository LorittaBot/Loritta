package net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft

import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.gabrielaimageserver.data.MinecraftSkinLorittaSweatshirtRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.gabrielaimageserver.exceptions.InvalidMinecraftSkinException
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.URLUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.I18N_CATEGORY_PREFIX
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.I18N_PREFIX
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.VALID_NAME_REGEX
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.convertNonDashedToUniqueID
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.mojang

class MCSkinLorittaSweatshirtExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    inner class Options : ApplicationCommandOptions() {
        val skin = string("skin", I18N_PREFIX.Sweatshirt.Options.Skin.Text)

        val sweatshirtStyle = string("sweatshirt_style", I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Text) {
            choice(
                I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.Light,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.LIGHT.name.lowercase()
            )
            choice(
                I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.Dark,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.DARK.name.lowercase()
            )
            choice(
                I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixWavy,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_WAVY.name.lowercase()
            )
            choice(
                I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixWavyWithStitches,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_WAVY_WITH_STITCHES.name.lowercase()
            )
            choice(
                I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixVertical,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_VERTICAL.name.lowercase()
            )
            choice(
                I18N_PREFIX.Sweatshirt.Options.SweatshirtStyle.Choice.MixVerticalWithStitches,
                MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.MIX_VERTICAL_WITH_STITCHES.name.lowercase()
            )
        }
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        val playerNameOrUrl = args[options.skin]
        val sweatshirtStyleName = args[options.sweatshirtStyle]

        val imageData = if (URLUtils.isValidHttpOrHttpsURL(playerNameOrUrl)) {
            URLImageData(playerNameOrUrl)
        } else {
            if (!playerNameOrUrl.matches(VALID_NAME_REGEX))
                context.fail(true) {
                    styled(
                        prefix = Emotes.Error,
                        content = context.i18nContext.get(
                            I18N_CATEGORY_PREFIX.InvalidPlayerName(
                                playerNameOrUrl
                            )
                        )
                    )
                }

            val profile = mojang.getUserProfileFromName(playerNameOrUrl) ?: context.fail(true) {
                styled(
                    prefix = Emotes.Error,
                    content = context.i18nContext.get(I18N_CATEGORY_PREFIX.UnknownPlayer(playerNameOrUrl))
                )
            }

            // Whether the player has the “Alex?” or “Steve?” skin depends on the Java hashCode of their UUID. Steve is used for even hashes. Example implementations:
            // https://wiki.vg/Mojang_API
            // TODO: This should be migrated to MinecraftMojangAPI instead of using hacky workarounds here
            val uniqueId = convertNonDashedToUniqueID(profile.profileId)
            val isSteve = uniqueId.hashCode() % 2 == 1

            val skinUrl = profile.textures["SKIN"]?.url ?: context.fail(true) {
                styled(
                    prefix = Emotes.Error,
                    content = context.i18nContext.get(
                        I18nKeysData.Commands.Command.Minecraft.Player.Skin.PlayerDoesNotHaveASkin(
                            playerName = playerNameOrUrl,
                            skinType = if (isSteve) "Steve" else "Alex"
                        )
                    )
                )
            }

            URLImageData(skinUrl)
        }

        val image = try {
            loritta.gabrielaImageServerClient.images.minecraftSkinLorittaSweatshirt(
                MinecraftSkinLorittaSweatshirtRequest(
                    imageData,
                    MinecraftSkinLorittaSweatshirtRequest.SweatshirtStyle.valueOf(sweatshirtStyleName.uppercase())
                )
            )
        } catch (e: InvalidMinecraftSkinException) {
            context.fail(true) {
                styled(
                    prefix = Emotes.Error,
                    content = context.i18nContext.get(
                        I18nKeysData.Commands.Command.Minecraft.Sweatshirt.InvalidMinecraftSkin
                    )
                )
            }
        }

        context.reply(true) {
            styled(
                context.i18nContext.get(
                    I18nKeysData.Commands.Command.Minecraft.Sweatshirt.Result
                ),
                Emotes.LoriHeart
            )

            files += FileUpload.fromData(image.inputStream(), "skin_lori_sweatshirt.png")
        }
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        val mutableArgs = args.toMutableList()
        val minecraftNickname = mutableArgs.removeFirstOrNull() ?: run {
            context.explain()

            return null
        }

        val style = mutableArgs.joinToString(" ")

        if (style.isBlank()) {
            context.explain()

            return null
        }

        val styles = hashMapOf(
            "claro" to "light",
            "escuro" to "dark",
            "misto diagonal" to "mix_wavy",
            "misto diagonal pontuado" to "mix_wavy_with_stitches",
            "misto vertical" to "mix_vertical",
            "misto vertical pontuado" to "mix_vertical_with_stitches"
        )

        val parsedStyle = styles[style] ?: run {
            context.explain()

            return null
        }

        return mapOf(
            options.skin to minecraftNickname,
            options.sweatshirtStyle to parsedStyle
        )
    }
}
