package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`

import net.perfectdreams.loritta.cinnamon.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled

class RateLoliExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        // Yes, this is meant to be unused
        val loli = string("loli", RateCommand.I18N_PREFIX.Loli.Options.Loli)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val strScore = "∞"
        val reason = context.i18nContext.get(
            RateCommand.I18N_PREFIX.WaifuHusbando.ScoreLoritta
        ).random() + " ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriYay}"

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

        context.giveAchievementAndNotify(net.perfectdreams.loritta.cinnamon.achievements.AchievementType.WEIRDO)
    }
}