package net.perfectdreams.loritta.commands

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.QuirkyStuff
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.IllegalFieldValueException
import java.time.ZonedDateTime

class BirthdayCommand(val m: QuirkyStuff) : LorittaDiscordCommand(arrayOf("birthday", "aniversário", "aniversario"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: BaseLocale) = locale["commands.social.birthday.description"]

	@Subcommand
	suspend fun root(context: DiscordCommandContext) {
		context.explain()
	}

	@Subcommand
	suspend fun setNewBirthday(context: DiscordCommandContext, locale: BaseLocale, newBirthday: String) {
		val split = newBirthday.split("/")

		if (split.size != 3) {
			context.reply(
					LoriReply(
							locale["commands.social.birthday.invalidDate"]
					)
			)
			return
		}

		val day = split[0].toInt()
		val month = split[1].toInt()
		val year = split[2].toInt()

		val currentYear = ZonedDateTime.now().year

		if (1900 > year) {
			context.reply(
					LoriReply(
							locale["commands.social.birthday.invalidDateYearTooOld"],
							Constants.ERROR
					)
			)
			return
		}

		if (year >= currentYear - 10) {
			context.reply(
					LoriReply(
							locale["commands.social.birthday.invalidDateYearTooNew"],
							Constants.ERROR
					)
			)
			return
		}

		val dateTime = try {
			DateTime(year, month, day, 0, 0)
		} catch (e: IllegalFieldValueException) {
			context.reply(
					LoriReply(
							locale["commands.social.birthday.invalidDate"]
					)
			)
			return
		}

		transaction(Databases.loritta) {
			context.lorittaUser.profile.settings.birthday = dateTime
		}

		context.reply(
				LoriReply(
						locale["commands.social.birthday.successfullyChanged"],
						"\uD83D\uDCC6"
				)
		)

		m.birthdaysRank?.updateBirthdayRanksForUser(context.userHandle)
	}
}