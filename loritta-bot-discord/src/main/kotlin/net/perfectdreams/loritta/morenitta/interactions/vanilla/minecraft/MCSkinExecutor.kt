package net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.I18N_CATEGORY_PREFIX
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.VALID_NAME_REGEX
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.convertNonDashedToUniqueID
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.mojang

open class MCSkinExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    inner class Options : ApplicationCommandOptions() {
        val username = string("player_name", I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        val player = args[options.username]
        if (!player.matches(VALID_NAME_REGEX))
            context.fail(true) {
                styled(
                    prefix = Emotes.Error,
                    content = context.i18nContext.get(I18N_CATEGORY_PREFIX.InvalidPlayerName(player))
                )
            }

        val profile = mojang.getUserProfileFromName(player) ?: context.fail(true) {
            styled(
                prefix = Emotes.Error,
                content = context.i18nContext.get(I18N_CATEGORY_PREFIX.UnknownPlayer(player))
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
                        playerName = player,
                        skinType = if (isSteve) "Steve" else "Alex"
                    )
                )
            )
        }

        context.reply(false) {
            content = skinUrl
        }
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        val username = args.getOrNull(0) ?: run {
            context.explain()

            return null
        }

        return mapOf(
            options.username to username
        )
    }
}