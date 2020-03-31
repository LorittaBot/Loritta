package net.perfectdreams.loritta.plugin.lorittabirthday2020.commands

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.magic.LoriToolsCommand
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.CollectedBirthday2020Points
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object GiveBirthdayKeysExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "give_birthday_keys_rewards"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "give_birthday_keys_rewards")
			return@task false

		var givenKeysCount = 0L

		val count = CollectedBirthday2020Points.points.count()
		transaction(Databases.loritta) {
			val results = CollectedBirthday2020Points.slice(CollectedBirthday2020Points.user, count)
					.selectAll()
					.groupBy(CollectedBirthday2020Points.user)
					.filter { it[count] >= 2000 }

			for (result in results) {
				val user = result[CollectedBirthday2020Points.user].value
				val points = result[count]

				val donationKeys = DonationKeys.select {
					DonationKeys.userId eq user and (DonationKeys.metadata.isNotNull())
				}.toList()

				val alreadyHasAKey = donationKeys.any {
					it[DonationKeys.metadata]!!.obj["type"].nullString == "LorittaBirthday2020Event"
				}

				if (!alreadyHasAKey) {
					DonationKeys.insert {
						it[userId] = user
						it[value] = 100.0
						it[expiresAt] = System.currentTimeMillis() + Constants.ONE_MONTH_IN_MILLISECONDS
						it[metadata] = jsonObject("type" to "LorittaBirthday2020Event")
					}
					givenKeysCount++
				}
			}
		}

		reply(
				LorittaReply(
						"Pronto! $givenKeysCount keys foram geradas"
				)
		)
		return@task true
	}
}