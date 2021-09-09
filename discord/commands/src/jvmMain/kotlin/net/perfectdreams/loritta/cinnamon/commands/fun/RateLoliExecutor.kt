package net.perfectdreams.loritta.cinnamon.commands.`fun`

import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.styled

class RateLoliExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(RateLoliExecutor::class) {
        object Options : CommandOptions() {
            // Yes, this is meant to be unused
            val loli = string("loli", RateCommand.I18N_PREFIX.Loli.Options.Loli)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val strScore = "∞"
        val reason = context.i18nContext.get(
            RateCommand.I18N_PREFIX.WaifuHusbando.ScoreLoritta
        ).random() + " ${emotes.loriYay}"

        context.sendMessage {
            styled(
                content = context.i18nContext.get(
                    RateCommand.I18N_PREFIX.Loli.IsThatATypo
                )
            )

            styled(
                content = context.i18nContext.get(
                    RateCommand.I18N_PREFIX.Result(
                        input = "Loritta",
                        score = strScore,
                        reason = reason
                    )
                ),
                prefix = "\uD83E\uDD14"
            )
        }
    }
}