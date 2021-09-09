package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.utils.ContextStringToUserNameConverter
import kotlin.random.Random

class RateWaifuExecutor() : CommandExecutor() {
    companion object : CommandExecutorDeclaration(RateWaifuExecutor::class) {
        object Options : CommandOptions() {
            val waifu = string("waifu", RateCommand.I18N_PREFIX.WaifuHusbando.Options.Waifu)
                .register()
        }

        override val options = Options

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
        suspend fun executeGeneric(input: String, context: CommandContext, typeSingular: String, typePlural: String) {
            val waifuLowerCase = input.lowercase()

            // Always use the same seed for the random generator, but change it every day
            val random = Random(Clock.System.now().toLocalDateTime(TimeZone.UTC).dayOfYear + waifuLowerCase.hashCode().toLong())
            val score = random.nextInt(0, 11)

            val scoreReason = context.i18nContext.get(
                RateCommand.waifuHusbandoScores(typeSingular, typePlural)[score]
            ).random()

            var reason = when (score) {
                10 -> "$scoreReason ${Emotes.loriWow}"
                9 -> "$scoreReason ${Emotes.loriHeart}"
                8 -> "$scoreReason ${Emotes.loriPat}"
                7 -> "$scoreReason ${Emotes.loriSmile}"
                3 -> "$scoreReason ${Emotes.loriShrug}"
                2 -> "$scoreReason ${Emotes.loriHmpf}"
                1 -> "$scoreReason ${Emotes.loriRage}"
                else -> scoreReason
            }

            var strScore = score.toString()
            when (waifuLowerCase) {
                "loritta", "lori" -> {
                    strScore = "∞"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreLoritta
                    ).random() + " ${Emotes.loriYay}"
                }
                "pantufa" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScorePantufa
                    ).random() + " ${Emotes.loriHeart}"
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
                    ).random() + " ${Emotes.loriCoffee}"
                }
                "carl-bot" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreCarlbot
                    ).random() + " ${Emotes.loriPat}"
                }
                "kuraminha" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreKuraminha(
                            typeSingular
                        )
                    ).random() + " ${Emotes.loriHm}"
                }
                "pollux" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScorePollux
                    ).random() + " ${Emotes.loriYay}"
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
                }
                "lorita", "lorrita" -> {
                    strScore = "-∞"
                    reason = context.i18nContext.get(
                        RateCommand.I18N_PREFIX.WaifuHusbando.ScoreLorrita
                    ).random() + " ${Emotes.loriHmpf}"
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
        }
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        executeGeneric(
            ContextStringToUserNameConverter.convert(context, args[options.waifu]),
            context,
                    RateCommand.WAIFU_SINGULAR,
            RateCommand.WAIFU_PLURAL
        )
    }
}