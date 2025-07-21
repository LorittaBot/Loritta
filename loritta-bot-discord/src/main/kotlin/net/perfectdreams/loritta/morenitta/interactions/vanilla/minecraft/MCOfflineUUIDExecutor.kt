package net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.I18N_CATEGORY_PREFIX
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.I18N_PREFIX
import java.util.UUID

class MCOfflineUUIDExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    inner class Options : ApplicationCommandOptions() {
        val username = string("player_name", I18N_CATEGORY_PREFIX.Options.PlayerNameJavaEdition)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        val player = args[options.username]

        val uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:$player").toByteArray(Charsets.UTF_8))

        context.reply(false) {
            styled(
                context.i18nContext.get(
                    I18N_PREFIX.Player.Offlineuuid.Result(
                        player,
                        uuid.toString()
                    )
                )
            )
        }
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        val username = context.args.getOrNull(0) ?: run {
            context.explain()

            return null
        }

        return mapOf(
            options.username to username
        )
    }
}