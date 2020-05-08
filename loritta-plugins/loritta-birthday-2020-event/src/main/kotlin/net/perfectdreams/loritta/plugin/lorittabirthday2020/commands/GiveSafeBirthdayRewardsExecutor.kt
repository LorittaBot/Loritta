package net.perfectdreams.loritta.plugin.lorittabirthday2020.commands

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Background
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.magic.LoriToolsCommand
import net.perfectdreams.loritta.plugin.lorittabirthday2020.LorittaBirthday2020
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Players
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.CollectedBirthday2020Points
import net.perfectdreams.loritta.plugin.lorittabirthday2020.utils.BirthdayTeam
import net.perfectdreams.loritta.tables.BackgroundPayments
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object GiveSafeBirthdayRewardsExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "give_safe_birthday_rewards"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "give_safe_birthday_rewards")
			return@task false

		var givenKeysCount = 0L
		var givenMissingBackgroundsCount = 0L

		val count = CollectedBirthday2020Points.points.count()
		transaction(Databases.loritta) {
			val results = CollectedBirthday2020Points.slice(CollectedBirthday2020Points.user, count)
					.selectAll()
					.groupBy(CollectedBirthday2020Points.user)
					.filter { it[count] >= 100 }


			for (result in results) {
				val user = result[CollectedBirthday2020Points.user].value
				val points = result[count]

				val playerEntry = Birthday2020Players.select {
					Birthday2020Players.user eq user
				}.first()

				val rewards = if (playerEntry[Birthday2020Players.team] == BirthdayTeam.PANTUFA) {
					LorittaBirthday2020.pantufaRewards
				} else {
					LorittaBirthday2020.gabrielaRewards
				}

				val newRewards = rewards.filter { it.requiredPoints in 100..points }

				val lorittaProfile = (loritta as Loritta).getOrCreateLorittaProfile(user)

				newRewards.forEach {
					if (it is LorittaBirthday2020.BackgroundReward) {
						val internalName = it.internalName

						val hasTheBackground = BackgroundPayments.select {
							BackgroundPayments.background eq internalName and (BackgroundPayments.userId eq lorittaProfile.id.value)
						}.count()

						if (hasTheBackground == 0L) {
							BackgroundPayments.insert {
								it[userId] = lorittaProfile.id.value
								it[cost] = 0
								it[background] = Background.findById(internalName)!!.id
								it[boughtAt] = System.currentTimeMillis()
							}
							givenMissingBackgroundsCount++
						}
					} else if (it is LorittaBirthday2020.PremiumKeyReward) {
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
			}
		}

		reply(
				LorittaReply(
						"Pronto! $givenKeysCount keys e $givenMissingBackgroundsCount backgrounds foram dados"
				)
		)
		return@task true
	}
}