package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled

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
        ).random() + " ${Emotes.LoriYay}"

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

        context.giveAchievementAndNotify(AchievementType.WEIRDO)
    }
}