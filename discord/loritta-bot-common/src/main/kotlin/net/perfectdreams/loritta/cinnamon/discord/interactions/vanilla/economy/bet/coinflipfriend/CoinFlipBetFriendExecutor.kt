package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet.coinflipfriend

import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.common.toMessageFormat
import dev.kord.core.entity.User
import kotlinx.datetime.Clock
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.SlashContextHighLevelEditableMessage
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.ShortenedToLongSonhosAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.BetCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class CoinFlipBetFriendExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = user("user", BetCommand.COINFLIP_FRIEND_I18N_PREFIX.Options.User.Text)
        val quantity = string("quantity", BetCommand.COINFLIP_FRIEND_I18N_PREFIX.Options.Quantity.Text) {
            autocomplete(ShortenedToLongSonhosAutocompleteExecutor(loritta))
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val receiver = args[options.user]
        val howMuch = args[options.quantity].toLongOrNull()
        val ttlDuration = 3.minutes
        val isLoritta = receiver.id == loritta.config.loritta.discord.applicationId

        // Too small
        if (howMuch == null || howMuch == 0L)
            context.failEphemerally(
                context.i18nContext.get(BetCommand.COINFLIP_FRIEND_I18N_PREFIX.TryingToBetZeroSonhos),
                Emotes.LoriHmpf
            )

        if (0L > howMuch)
            context.failEphemerally(
                context.i18nContext.get(BetCommand.COINFLIP_FRIEND_I18N_PREFIX.TryingToBetLessThanZeroSonhos),
                Emotes.LoriHmpf
            )

        if (context.user.id == receiver.id)
            context.failEphemerally(
                context.i18nContext.get(BetCommand.COINFLIP_FRIEND_I18N_PREFIX.CantBetSelf),
                Emotes.Error
            )

        loritta.coinFlipBetUtils.createBet(
            context,
            howMuch,
            receiver.id,
            0
        )
    }
}