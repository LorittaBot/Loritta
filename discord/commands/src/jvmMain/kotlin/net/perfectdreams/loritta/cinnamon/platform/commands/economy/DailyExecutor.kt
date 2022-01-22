package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.GACampaigns
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.DailyCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.utils.SonhosUtils.userHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(DailyExecutor::class)

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        // TODO: Do not hardcode the timezone
        val now = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        val todayDailyReward = context.loritta.services.sonhos.getUserLastDailyRewardReceived(
            UserId(context.user.id.value),
            now.toKotlinLocalDateTime().toInstant(TimeZone.of("America/Sao_Paulo"))
        )

        val tomorrowAtMidnight = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .plusDays(1L)

        if (todayDailyReward != null) {
            context.sendEphemeralMessage {
                styled(
                    context.i18nContext.get(DailyCommand.I18N_PREFIX.PleaseWait("<t:${tomorrowAtMidnight.toInstant().toEpochMilli() / 1000}:R>")),
                    Emotes.Error
                )

                userHaventGotDailyTodayOrUpsellSonhosBundles(
                    context.loritta,
                    context.i18nContext,
                    UserId(context.user.id.value),
                    "daily",
                    "please-wait-daily-reset"
                )
            }
            return
        }

        val url = if (context is GuildApplicationCommandContext)
            "${context.loritta.config.website}daily?guild=${context.guildId.value}"
        else // Used for daily multiplier priority
            "${context.loritta.config.website}daily"

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(DailyCommand.I18N_PREFIX.DailyLink(url, "<t:${tomorrowAtMidnight.toInstant().toEpochMilli() / 1000}:t>")),
                Emotes.LoriRich
            )

            styled(
                context.i18nContext.get(DailyCommand.I18N_PREFIX.DailyWarning("${context.loritta.config.website}guidelines")),
                Emotes.LoriBanHammer
            )

            styled(
                context.i18nContext.get(
                    GACampaigns.sonhosBundlesUpsellDiscordMessage(
                        context.loritta.config.website,
                        "daily",
                        "daily-reward"
                    )
                ),
                Emotes.CreditCard
            )
        }
    }
}