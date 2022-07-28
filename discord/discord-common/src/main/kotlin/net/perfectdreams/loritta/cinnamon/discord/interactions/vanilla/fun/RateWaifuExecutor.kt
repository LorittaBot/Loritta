package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.perfectdreams.loritta.cinnamon.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.utils.ContextStringToUserNameConverter
import kotlin.random.Random

class RateWaifuExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    companion object {
        /**
         * Executes the generic Waifu/Husbando score response.
         *
         * Because they are similar (the only thing that are different is the [typeSingular] and [typePlural], this should be used to avoid
         * code duplication.
         *
         * @param input        the input that will be scored
         * @param context      the command context
         * @param emotes       the emotes, used in the messages
         * @param typeSingular the singular type of the category that is being scored
         * @param typePlural   the plural type of the category that is being scored
         */
        suspend fun executeGeneric(input: String, context: ApplicationCommandContext, typeSingular: String, typePlural: String) {
            val waifuLowerCase = input.lowercase()

            // Always use the same seed for the random generator, but change it every day
            val random = Random(Clock.System.now().toLocalDateTime(TimeZone.UTC).dayOfYear + waifuLowerCase.hashCode().toLong())
            val score = random.nextInt(0, 11)

            val scoreReason = context.i18nContext.get(
                RateCommand.waifuHusbandoScores(typeSingular, typePlural)[score]
            ).random()

            var reason = when (score) {
                10 -> "$scoreReason ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriWow}"
                9 -> "$scoreReason ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHeart}"
                8 -> "$scoreReason ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriPat}"
                7 -> "$scoreReason ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSmile}"
                3 -> "$scoreReason ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriShrug}"
                2 -> "$scoreReason ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHmpf}"
                1 -> "$scoreReason ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriRage}"
                else -> scoreReason
            }

            var strScore = score.toString()
            var isLoritta = false
            var isGroovy = false

            when (waifuLowerCase) {
                "loritta", "lori" -> {
                    strScore = "∞"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreLoritta
                    ).random() + " ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriYay}"
                    isLoritta = true
                }
                "pantufa" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScorePantufa
                    ).random() + " ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHeart}"
                }
                "wumpus" -> {
                    strScore = "∞"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreWumpus
                    ).random()
                }
                "erisly" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreErisly
                    ).random()
                }
                "dank memer" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreDankMemer(typeSingular)
                    ).random() + " ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriCoffee}"
                }
                "carl-bot" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreCarlbot
                    ).random() + " ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriPat}"
                }
                "kuraminha" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreKuraminha(
                            typeSingular
                        )
                    ).random() + " ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHm}"
                }
                "pollux" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScorePollux
                    ).random() + " ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriYay}"
                }
                "tatsumaki" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreTatsumaki(RateCommand.WAIFU_SINGULAR)
                    ).random()
                }
                "mee6" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreMee6(RateCommand.WAIFU_SINGULAR)
                    ).random()
                }
                "mantaro" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreMantaro
                    ).random()
                }
                "dyno" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreDyno(RateCommand.WAIFU_SINGULAR)
                    ).random()
                }
                "mudae" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreMudae
                    ).random()
                }
                "nadeko" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreNadeko(RateCommand.WAIFU_SINGULAR)
                    ).random()
                }
                "unbelievaboat" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreUnbelievaBoat
                    ).random()
                }
                "chino kafuu" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreChinoKafuu
                    ).random()
                }
                "groovy" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreGroovy
                    ).random()
                    isGroovy = true
                }
                "lorita", "lorrita" -> {
                    strScore = "-∞"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreLorrita
                    ).random() + " ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHmpf}"
                }
            }

            // TODO: Fix stripCodeMarks, maybe implement a safer way to sanitize user input?
            context.sendReply(
                content = context.i18nContext.get(
                    RateCommand.I18N_PREFIX.Result(
                        input = input,
                        score = strScore,
                        reason = reason
                    )
                ),
                prefix = "\uD83E\uDD14"
            )

            if (isLoritta)
                context.giveAchievementAndNotify(net.perfectdreams.loritta.cinnamon.achievements.AchievementType.INFLATED_EGO)
            if (isGroovy)
                context.giveAchievementAndNotify(net.perfectdreams.loritta.cinnamon.achievements.AchievementType.PRESS_PLAY_TO_PAY_RESPECTS)
        }
    }

    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val waifu = string("waifu", RateCommand.I18N_PREFIX.WaifuHusbando.Options.Waifu)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        executeGeneric(
            ContextStringToUserNameConverter.convert(context, args[options.waifu]),
            context,
            RateCommand.WAIFU_SINGULAR,
            RateCommand.WAIFU_PLURAL
        )
    }
}