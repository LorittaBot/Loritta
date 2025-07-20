package net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.I18N_PREFIX

class SparklyPowerExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.reply(true) {
            styled(
                context.i18nContext.get(I18N_PREFIX.Sparklypower.Response1),
                Emotes.PantufaPickaxe
            )
            styled(
                context.i18nContext.get(I18N_PREFIX.Sparklypower.Response2("https://discord.gg/sparklypower"))
            )
        }
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        return null
    }
}