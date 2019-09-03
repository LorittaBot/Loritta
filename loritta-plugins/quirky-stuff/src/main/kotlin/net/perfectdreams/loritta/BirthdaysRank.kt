package net.perfectdreams.loritta

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.BirthdayConfigs
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.tables.UserSettings
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.editMessageIfContentWasChanged
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.utils.CalendarUtils
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

class BirthdaysRank(val m: QuirkyStuff, val config: QuirkyConfig) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var task: Job? = null

	fun start() {
		logger.info { "Starting Birthday Rank Task..." }

		task = GlobalScope.launch(LorittaLauncher.loritta.coroutineDispatcher) {
			while (true) {
				val timeUntilMidnight = CalendarUtils.getTimeUntilMidnight()
				logger.info { "Waiting ${timeUntilMidnight}ms until midnight to update the birthday rank task"}
				delay(timeUntilMidnight)
				updateAllBirthdayRanks()
			}
		}
	}

	suspend fun updateBirthdayRanksForUser(user: User) {
		val mutualGuilds = lorittaShards.getMutualGuilds(user)

		mutualGuilds.forEach {
			updateBirthdayRank(it)
		}
	}

	suspend fun updateAllBirthdayRanks() {
		val resultRows = transaction(Databases.loritta) {
			(ServerConfigs innerJoin BirthdayConfigs)
					.select {
						BirthdayConfigs.enabled eq true
					}
		}

		for (resultRow in resultRows) {
			val guildId = resultRow[ServerConfigs.id].value
			val guild = lorittaShards.getGuildById(guildId) ?: continue
			val serverConfig = loritta.getServerConfigForGuild(guildId.toString())

			updateBirthdayRank(guild, serverConfig, resultRow)
		}
	}

	suspend fun updateBirthdayRank(guild: Guild) = updateBirthdayRank(guild, loritta.getServerConfigForGuild(guild.id))

	suspend fun updateBirthdayRank(guild: Guild, serverConfig: MongoServerConfig) {
		val config = transaction(Databases.loritta) {
			(ServerConfigs innerJoin BirthdayConfigs)
					.select {
						BirthdayConfigs.enabled eq true and (ServerConfigs.id eq guild.idLong)
					}
					.firstOrNull()
		} ?: return

		logger.info { "Updating birthday rank in $guild..." }

		updateBirthdayRank(guild, serverConfig, config)
	}

	suspend  fun updateBirthdayRank(guild: Guild, serverConfig: MongoServerConfig, config: ResultRow) {
		val channelId = config[BirthdayConfigs.channelId] ?: return
		val channel = guild.getTextChannelById(channelId) ?: return

		val profiles = transaction(Databases.loritta) {
			(Profiles innerJoin UserSettings)
					.select {
						UserSettings.birthday.isNotNull() // TODO: Melhorar verificação para pegar apenas o que a gente realmente se interessa
					}
					.toMutableList()
		}

		val locale = loritta.getLocaleById(serverConfig.localeId)

		// Matching Users = People that are on the server, wow!
		val matchingUsers = profiles.filter { guild.getMemberById(it[Profiles.id].value) != null }

		val usersHavingBirthdayToday = getUsersHavingBirthdayOnDate(matchingUsers, 0)
		val usersHavingInOneDay = getUsersHavingBirthdayOnDate(matchingUsers, 1)
		val usersHavingInTwoDays = getUsersHavingBirthdayOnDate(matchingUsers, 2)
		val usersHavingInThreeDays = getUsersHavingBirthdayOnDate(matchingUsers, 3)
		val usersHavingInFourDays = getUsersHavingBirthdayOnDate(matchingUsers, 4)
		val usersHavingInFiveDays = getUsersHavingBirthdayOnDate(matchingUsers, 5)
		val usersHavingInSixDays = getUsersHavingBirthdayOnDate(matchingUsers, 6)

		val roles = config[BirthdayConfigs.roles]
		if (roles != null) {
			val birthdayRoles = roles.mapNotNull { guild.getRoleById(it) }

			if (birthdayRoles.isNotEmpty()) {
				// Remover cargos de usuários que não estão fazendo mais aniversário
				val membersWithBirthdayRoles = guild.getMembersWithRoles(birthdayRoles)

				val usersWithBirthdayRolesButShouldntHaveThem = membersWithBirthdayRoles.filterNot { member ->
					usersHavingBirthdayToday.any {
						it[Profiles.id].value == member.idLong
					}
				}

				usersWithBirthdayRolesButShouldntHaveThem.forEach {
					guild.modifyMemberRoles(
							it,
							it.roles.toMutableList().apply {
								this.removeAll(birthdayRoles)
							}
					).await()
				}

				// Dar cargos de aniversariantes para quem merece
				usersHavingBirthdayToday.forEach {
					val member = guild.getMemberById(it[Profiles.id].value) ?: return@forEach

					val rolesMissing = birthdayRoles.filterNot { member.roles.contains(it) }

					if (rolesMissing.isNotEmpty())
						guild.modifyMemberRoles(
								member,
								member.roles.toMutableList().apply {
									this.addAll(rolesMissing)
								}
						).await()
				}
			}
		}

		val messages = channel.history.retrievePast(7).await()

		val todayBirthday = messages.getOrNull(0)
		val inOneDay = messages.getOrNull(1)
		val inTwoDays = messages.getOrNull(2)
		val inThreeDays = messages.getOrNull(3)
		val inFourDays = messages.getOrNull(4)
		val inFiveDays = messages.getOrNull(5)
		val inSixDays = messages.getOrNull(6)


		editMessageOrSendIfNull(
				buildBirthdayMessage("<:lori_sorriso:556525532359950337> **${locale["loritta.modules.birthday.usersBirthdayInXDaysPlural", 6]}:** ", usersHavingInSixDays, locale, serverConfig.commandPrefix),
				channel,
				inSixDays
		)

		editMessageOrSendIfNull(
				buildBirthdayMessage("<:lori_happy:585550787426648084> **${locale["loritta.modules.birthday.usersBirthdayInXDaysPlural", 5]}:** ", usersHavingInFiveDays, locale, serverConfig.commandPrefix),
				channel,
				inFiveDays
		)

		editMessageOrSendIfNull(
				buildBirthdayMessage("<:lori_cute:585549695737266186> **${locale["loritta.modules.birthday.usersBirthdayInXDaysPlural", 4]}:** ", usersHavingInFourDays, locale, serverConfig.commandPrefix),
				channel,
				inFourDays
		)

		editMessageOrSendIfNull(
				buildBirthdayMessage("<:lori_very_owo:562303822978875403> **${locale["loritta.modules.birthday.usersBirthdayInXDaysPlural", 3]}:** ", usersHavingInThreeDays, locale, serverConfig.commandPrefix),
				channel,
				inThreeDays
		)

		editMessageOrSendIfNull(
				buildBirthdayMessage("<a:lori_temmie:515330130495799307> **${locale["loritta.modules.birthday.usersBirthdayInXDaysPlural", 2]}:** ", usersHavingInTwoDays, locale, serverConfig.commandPrefix),
				channel,
				inTwoDays
		)

		editMessageOrSendIfNull(
				buildBirthdayMessage("<a:owo_whats_this:515329346194636811> **${locale["loritta.modules.birthday.usersBirthdayTomorrow"]}:** ", usersHavingInOneDay, locale, serverConfig.commandPrefix),
				channel,
				inOneDay
		)

		editMessageOrSendIfNull(
				buildBirthdayMessage("\uD83C\uDF70 **${locale["loritta.modules.birthday.usersBirthdayToday"]}:** ", usersHavingBirthdayToday, locale, serverConfig.commandPrefix),
				channel,
				todayBirthday
		)
	}


	private fun getUsersHavingBirthdayOnDate(users: List<ResultRow>, skipDays: Int): List<ResultRow> {
		return users.filter {
			val date = it[UserSettings.birthday] ?: return@filter false

			val beginningOfTheDay = DateTime.now().toMutableDateTime()
					.dayOfYear().add(skipDays)

			// Nós utilizamos "isEqual" já que o tempo carregado pelo Exposed sempre será "00:00"
			date.monthOfYear().get() == beginningOfTheDay.monthOfYear().get() && date.dayOfMonth().get() == beginningOfTheDay.dayOfMonth().get()
		}
	}

	private fun buildBirthdayMessage(message: String, users: List<ResultRow>, locale: BaseLocale, commandPrefix: String): String {
		val stringBuilder = StringBuilder()
		stringBuilder.append(message)
		if (users.isEmpty()) {
			stringBuilder.append("*")
			stringBuilder.append(
					locale[
							"loritta.modules.birthday.nobodyHasBirthdaySetYourOwn",
							"`" + locale["loritta.modules.birthday.birthdayCommandExample", commandPrefix] + "`"
					]
			)
			stringBuilder.append("*")
		} else
			stringBuilder.append(
					users.joinToString(", ",
							transform = {
								val birthday = DateTime.now().year - (it[UserSettings.birthday]?.year ?: 0)
								locale["loritta.modules.birthday.inlineBirthday", "<@${it[Profiles.id].value}>", birthday]
							}
					)
			)

		return stringBuilder.toString()
	}

	private suspend fun editMessageOrSendIfNull(text: String, channel: TextChannel, message: Message?) {
		if (message != null) {
			message.editMessageIfContentWasChanged(text)
		} else {
			channel.sendMessage(text).await()
		}
	}
}