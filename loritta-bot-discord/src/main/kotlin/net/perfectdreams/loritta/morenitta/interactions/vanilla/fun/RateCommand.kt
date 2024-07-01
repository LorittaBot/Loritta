package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.ContextStringToUserNameConverter
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import kotlin.random.Random

class RateCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Rate
        const val WAIFU_SINGULAR = "Waifu"
        const val WAIFU_PLURAL = "Waifus"

        const val HUSBANDO_SINGULAR = "Husbando"
        const val HUSBANDO_PLURAL = "Husbandos"

        private fun waifuHusbandoScores(type: String, typePlural: String) = listOf(
            I18N_PREFIX.WaifuHusbando.Score0(type = type),
            I18N_PREFIX.WaifuHusbando.Score1(type = type),
            I18N_PREFIX.WaifuHusbando.Score2(type = type),
            I18N_PREFIX.WaifuHusbando.Score3(typePlural = typePlural),
            I18N_PREFIX.WaifuHusbando.Score4(type = type),
            I18N_PREFIX.WaifuHusbando.Score5,
            I18N_PREFIX.WaifuHusbando.Score6(type = type),
            I18N_PREFIX.WaifuHusbando.Score7(typePlural = typePlural),
            I18N_PREFIX.WaifuHusbando.Score8(type = type),
            I18N_PREFIX.WaifuHusbando.Score9(type = type),
            I18N_PREFIX.WaifuHusbando.Score10(type = type),
        )

        suspend fun executeGeneric(input: String, context: UnleashedContext, typeSingular: String, typePlural: String) {
            val waifuLowerCase = input.lowercase()

            val random = Random(Clock.System.now().toLocalDateTime(TimeZone.UTC).dayOfYear + waifuLowerCase.hashCode().toLong())
            val score = random.nextInt(0, 11)

            val scoreReason = context.i18nContext.get(
                waifuHusbandoScores(typeSingular, typePlural)[score]
            ).random()

            var reason = when (score) {
                10 -> "$scoreReason ${Emotes.LoriWow}"
                9 -> "$scoreReason ${Emotes.LoriHeart}"
                8 -> "$scoreReason ${Emotes.LoriPat}"
                7 -> "$scoreReason ${Emotes.LoriSmile}"
                3 -> "$scoreReason ${Emotes.LoriShrug}"
                2 -> "$scoreReason ${Emotes.LoriHmpf}"
                1 -> "$scoreReason ${Emotes.LoriRage}"
                else -> scoreReason
            }

            var strScore = score.toString()
            var isLoritta = false
            var isGroovy = false

            when (waifuLowerCase) {
                "loritta", "lori" -> {
                    strScore = "∞"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreLoritta
                    ).random() + " ${Emotes.LoriYay}"
                    isLoritta = true
                }
                "pantufa" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScorePantufa
                    ).random() + " ${Emotes.LoriHeart}"
                }
                "wumpus" -> {
                    strScore = "∞"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreWumpus
                    ).random()
                }
                "erisly" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreErisly
                    ).random()
                }
                "dank memer" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreDankMemer(typeSingular)
                    ).random() + " ${Emotes.LoriCoffee}"
                }
                "carl-bot" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreCarlbot
                    ).random() + " ${Emotes.LoriPat}"
                }
                "kuraminha" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreKuraminha(
                            typeSingular
                        )
                    ).random() + " ${Emotes.LoriHm}"
                }
                "pollux" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScorePollux
                    ).random() + " ${Emotes.LoriYay}"
                }
                "tatsumaki" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreTatsumaki(RateCommand.WAIFU_SINGULAR)
                    ).random()
                }
                "mee6" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreMee6(RateCommand.WAIFU_SINGULAR)
                    ).random()
                }
                "mantaro" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreMantaro
                    ).random()
                }
                "dyno" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreDyno(RateCommand.WAIFU_SINGULAR)
                    ).random()
                }
                "mudae" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreMudae
                    ).random()
                }
                "nadeko" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreNadeko(RateCommand.WAIFU_SINGULAR)
                    ).random()
                }
                "unbelievaboat" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreUnbelievaBoat
                    ).random()
                }
                "chino kafuu" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreChinoKafuu
                    ).random()
                }
                "groovy" -> {
                    strScore = "10"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreGroovy
                    ).random()
                    isGroovy = true
                }
                "lorita", "lorrita" -> {
                    strScore = "-∞"
                    reason = context.i18nContext.get(
                        I18N_PREFIX.WaifuHusbando.ScoreLorrita
                    ).random() + " ${Emotes.LoriHmpf}"
                }
            }

            context.reply(false) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Result(
                            input = input,
                            score = strScore,
                            reason = reason
                        )
                    ),
                    "\uD83E\uDD14"
                )
            }

            if (isLoritta)
                context.giveAchievementAndNotify(AchievementType.INFLATED_EGO, true)
            if (isGroovy)
                context.giveAchievementAndNotify(AchievementType.PRESS_PLAY_TO_PAY_RESPECTS, true)
        }
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.WaifuHusbando.WaifuLabel, I18N_PREFIX.WaifuHusbando.Description(WAIFU_SINGULAR)) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("ratewaifu")
            }

            executor = RateWaifuExecutor()
        }

        subcommand(I18N_PREFIX.WaifuHusbando.HusbandoLabel, I18N_PREFIX.WaifuHusbando.Description(HUSBANDO_SINGULAR)) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("ratehusbando")
            }

            executor = RateHusbandoExecutor()
        }

        subcommand(I18N_PREFIX.Loli.Label, I18N_PREFIX.Loli.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("rateloli")
            }

            executor = RateLoliExecutor()
        }
    }

    inner class RateWaifuExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val waifu = string("waifu", I18N_PREFIX.WaifuHusbando.Options.Waifu)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            executeGeneric(
                ContextStringToUserNameConverter.convert(context, args[options.waifu]),
                context,
                WAIFU_SINGULAR,
                WAIFU_PLURAL
            )
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val waifu = args.getOrNull(0)

            if (waifu == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.waifu to waifu
            )
        }
    }

    inner class RateHusbandoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val husbando = string("husbando", I18N_PREFIX.WaifuHusbando.Options.Husbando)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            executeGeneric(
                ContextStringToUserNameConverter.convert(context, args[options.husbando]),
                context,
                HUSBANDO_SINGULAR,
                HUSBANDO_PLURAL
            )
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val husbando = args.getOrNull(0)

            if (husbando == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.husbando to husbando
            )
        }
    }

    inner class RateLoliExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val loli = string("loli", I18N_PREFIX.Loli.Options.Loli)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val strScore = "∞"
            val reason = context.i18nContext.get(
                I18N_PREFIX.WaifuHusbando.ScoreLoritta
            ).random() + "${Emotes.LoriYay}"

            context.reply(false) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Loli.IsThatATypo
                    )
                )

                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Result(
                            input = "Loritta",
                            score = strScore,
                            reason = reason
                        )
                    ),
                    "\uD83E\uDD14"
                )
            }

            context.giveAchievementAndNotify(AchievementType.WEIRDO, true)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }
    }
}