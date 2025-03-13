package net.perfectdreams.loritta.morenitta.utils

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import java.time.Instant
import kotlin.time.Duration.Companion.days

object VacationModeUtils {
    const val VACATION_DISABLE_COST = 5_000_000L
    val minimumLength = 2.days
    val maximumLength = 180.days
    val lengthBetweenVacations = 7.days

    /**
     * Checks if the user involved in the current [context] is on vacation mode. If they are, a message is sent
     *
     * @param context the context
     * @param ephemeral if the message should be ephemeral
     * @return if the user is on vacation, it will return true
     */
    suspend fun checkIfWeAreOnVacation(context: UnleashedContext, ephemeral: Boolean): Boolean {
        // Are we in vacation?
        val vacationUntil = context.lorittaUser.profile.vacationUntil
        if (vacationUntil != null && vacationUntil > Instant.now()) {
            // Yeah, we are!
            context.reply(ephemeral) {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.Command.Vacation.YouAreOnVacation(TimeFormat.DATE_TIME_LONG.format(vacationUntil))),
                    Emotes.LoriSleeping
                )
            }
            return true
        }

        return false
    }

    /**
     * Checks if the [user] is on vacation mode. If they are, a message is sent
     *
     * @param context the context
     * @param user the user to be checked
     * @param ephemeral if the message should be ephemeral
     * @return if the user is on vacation, it will return true
     */
    suspend fun checkIfUserIsOnVacation(context: UnleashedContext, user: User, ephemeral: Boolean): Boolean {
        if (user == context.user)
            return checkIfWeAreOnVacation(context, ephemeral)

        // Are they on vacation?
        val profile = context.loritta.getLorittaProfile(user.idLong) ?: return false

        val vacationUntil = profile.vacationUntil
        if (vacationUntil != null && vacationUntil > Instant.now()) {
            // Yeah, they are!
            context.reply(ephemeral) {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.Command.Vacation.UserIsOnVacation(user.asMention, TimeFormat.DATE_TIME_LONG.format(vacationUntil))),
                    Emotes.LoriSleeping
                )
            }
            return true
        }

        return false
    }

    fun isOnVacation(vacationUntil: Instant?): Boolean {
        val now = Instant.now()
        return vacationUntil != null && vacationUntil > now
    }
}