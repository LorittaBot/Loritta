package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.TimeUtils
import net.perfectdreams.loritta.morenitta.utils.VacationModeUtils
import net.perfectdreams.loritta.serializable.StoredVacationModeLeaveTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.time.toKotlinDuration

class VacationCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Vacation
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("8308a79b-4003-4dd8-938a-efdbfe46edd1")) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommand(I18N_PREFIX.Enable.Label, I18N_PREFIX.Enable.Description, UUID.fromString("c49ab9f5-54c4-49b1-a24f-61a06b807f4d")) {
            executor = VacationEnableExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Disable.Label, I18N_PREFIX.Disable.Description, UUID.fromString("258aff54-52cc-4b5e-b392-3c60f892c6ba")) {
            executor = VacationDisableExecutor(loritta)
        }
    }

    class VacationEnableExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val duration = string("duration", I18N_PREFIX.Enable.Options.Duration.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val user = context.user
            val durationAsString = args[options.duration]
            val now = Instant.now()
            val whenItWillExpireDuration = TimeUtils.convertToMillisDurationRelative(durationAsString)

            val vacationUntil = context.lorittaUser.profile.vacationUntil
            if (vacationUntil != null && vacationUntil > now) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.YouAreOnVacation(TimeFormat.DATE_TIME_LONG.format(vacationUntil))
                        ),
                        Emotes.BeachWithUmbrella
                    )
                }
            } else {
                if (vacationUntil != null) {
                    // The "vacationUntil" is in the past here
                    val duration = Duration.between(vacationUntil, now)
                        .toKotlinDuration()

                    if (VacationModeUtils.lengthBetweenVacations > duration) {
                        context.reply(false) {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.Enable.YouNeedToWaitBeforeYourNextVacation(TimeFormat.DATE_TIME_LONG.format(now.plusMillis(VacationModeUtils.lengthBetweenVacations.inWholeMilliseconds)))),
                                Emotes.Error
                            )
                        }
                        return
                    }
                }

                val whenItWillExpire = now.plus(whenItWillExpireDuration)
                val duration = Duration.between(now, whenItWillExpire)
                    .toKotlinDuration()

                if (VacationModeUtils.minimumLength > duration) {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Enable.VacationTooShort),
                            Emotes.Error
                        )
                    }
                    return
                }

                if (duration > VacationModeUtils.maximumLength) {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Enable.VacationTooLong),
                            Emotes.Error
                        )
                    }
                    return
                }

                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Enable.PayAttention),
                        Emotes.LoriBanHammer
                    )

                    styled(context.i18nContext.get(I18N_PREFIX.Enable.EnablingVacation))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.Explanation))

                    styled("")

                    styled(
                        context.i18nContext.get(I18N_PREFIX.Enable.WhatYouCanDo),
                        Emotes.LoriCoffee
                    )

                    styled(context.i18nContext.get(I18N_PREFIX.Enable.GetTheDailyReward))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.BuyTrinkets))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.BuyBundles))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.UseCommand(loritta.commandMentions.brokerBuy)))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.UseCommand(loritta.commandMentions.brokerSell)))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.EnjoyYourVacations))

                    styled("")

                    styled(
                        context.i18nContext.get(I18N_PREFIX.Enable.WhatYouCantDo),
                        Emotes.LoriHmpf
                    )

                    styled(context.i18nContext.get(I18N_PREFIX.Enable.SendOrReceiveSonhos))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.UseCommand(loritta.commandMentions.coinflipBet)))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.UseCommand(loritta.commandMentions.coinflipBetGlobal)))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.UseCommand(loritta.commandMentions.emojiFightStart)))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.UseCommand(loritta.commandMentions.raffleBuy)))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.SparklyPowerLsxTransfer))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.ApiTransfer))
                    styled(context.i18nContext.get(I18N_PREFIX.Enable.AndOtherThings))

                    styled("")

                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.Enable.Tax(
                                TimeFormat.DATE_TIME_LONG.format(whenItWillExpire),
                                SonhosUtils.getSonhosEmojiOfQuantity(VacationModeUtils.VACATION_DISABLE_COST),
                                VacationModeUtils.VACATION_DISABLE_COST
                            )
                        )
                    )

                    actionRow(
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            ButtonStyle.DANGER,
                            context.i18nContext.get(I18N_PREFIX.Enable.EnableVacation),
                            {
                                loriEmoji = Emotes.BeachWithUmbrella
                            }
                        ) {
                            it.invalidateComponentCallback()

                            val context = it.deferChannelMessage(false)

                            loritta.pudding.transaction {
                                Profiles.update({ Profiles.id eq user.idLong }) {
                                    it[Profiles.vacationUntil] = whenItWillExpire
                                }
                            }

                            context.editOriginal {
                                styled(
                                    it.i18nContext.get(I18N_PREFIX.Enable.VacationModeEnabled),
                                    Emotes.LoriSunglasses
                                )
                            }
                        }
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(options.duration to args.joinToString(" "))
        }
    }

    class VacationDisableExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val user = context.user

            val vacationUntil = context.lorittaUser.profile.vacationUntil
            if (vacationUntil != null && vacationUntil > Instant.now()) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.Disable.DisablingBeforeTime(
                                TimeFormat.DATE_TIME_LONG.format(vacationUntil),
                                SonhosUtils.getSonhosEmojiOfQuantity(VacationModeUtils.VACATION_DISABLE_COST),
                                VacationModeUtils.VACATION_DISABLE_COST
                            )
                        )
                    )

                    actionRow(
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            ButtonStyle.DANGER,
                            context.i18nContext.get(I18N_PREFIX.Disable.LeaveVacation),
                            {
                                loriEmoji = Emotes.LoriHmpf
                            }
                        ) {
                            it.invalidateComponentCallback()

                            val hook = it.deferChannelMessage(false)

                            val result = loritta.pudding.transaction {
                                val now = Instant.now()

                                val profile = Profiles.selectAll().where {
                                    Profiles.id eq it.user.idLong
                                }.limit(1).firstOrNull()

                                if (profile == null)
                                    return@transaction Result.NotInVacation

                                val vacationUntil = profile[Profiles.vacationUntil]
                                if (vacationUntil == null || now >= vacationUntil)
                                    return@transaction Result.NotInVacation

                                val sonhos = profile[Profiles.money]

                                if (VacationModeUtils.VACATION_DISABLE_COST > sonhos)
                                    return@transaction Result.NotEnoughSonhos(sonhos)

                                // Cinnamon transaction log
                                SimpleSonhosTransactionsLogUtils.insert(
                                    user.idLong,
                                    now,
                                    TransactionType.VACATION_MODE,
                                    VacationModeUtils.VACATION_DISABLE_COST,
                                    StoredVacationModeLeaveTransaction
                                )

                                Profiles.update({ Profiles.id eq user.idLong }) {
                                    with(SqlExpressionBuilder) {
                                        it[Profiles.vacationUntil] = now
                                        it[Profiles.money] = Profiles.money - VacationModeUtils.VACATION_DISABLE_COST
                                    }
                                }

                                return@transaction Result.Success
                            }

                            when (result) {
                                is Result.NotEnoughSonhos -> {
                                    hook.editOriginal {
                                        styled(
                                            context.i18nContext.get(SonhosUtils.insufficientSonhos(result.userSonhos, VacationModeUtils.VACATION_DISABLE_COST)),
                                            Emotes.Error
                                        )
                                    }
                                }
                                Result.NotInVacation -> {
                                    hook.editOriginal {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.Disable.YouArentInVacation),
                                            Emotes.Error
                                        )
                                    }
                                }
                                Result.Success -> {
                                    hook.editOriginal {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.Disable.RemovedVacationMode)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                return
            } else {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Disable.YouArentInVacation),
                        Emotes.Error
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?>? = LorittaLegacyMessageCommandExecutor.NO_ARGS

        private sealed class Result {
            data object Success : Result()
            data class NotEnoughSonhos(val userSonhos: Long) : Result()
            data object NotInVacation : Result()
        }
    }
}