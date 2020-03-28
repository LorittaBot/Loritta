package net.perfectdreams.loritta.plugin.lorittabirthday2020.commands

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Background
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.commands.vanilla.magic.LoriToolsCommand
import net.perfectdreams.loritta.plugin.lorittabirthday2020.LorittaBirthday2020
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Players
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.CollectedBirthday2020Points
import net.perfectdreams.loritta.plugin.lorittabirthday2020.utils.BirthdayTeam
import net.perfectdreams.loritta.tables.BackgroundPayments
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object GiveBirthdayRewardsExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "give_new_birthday_rewards"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "give_new_birthday_rewards")
			return@task false

		val count = CollectedBirthday2020Points.points.count()
		transaction(Databases.loritta) {
			val results = CollectedBirthday2020Points.slice(CollectedBirthday2020Points.user, count)
					.selectAll()
					.groupBy(CollectedBirthday2020Points.user)
					.filter { it[count] >= 1350 }


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

				val newRewards = rewards.filter { it.requiredPoints in 1380..points }

				val lorittaProfile = (loritta as Loritta).getOrCreateLorittaProfile(user)

				newRewards.forEach {
					if (it is LorittaBirthday2020.SonhosReward) {
						lorittaProfile.money += it.sonhosReward
					} else if (it is LorittaBirthday2020.BackgroundReward) {
						val internalName = it.internalName
						BackgroundPayments.insert {
							it[userId] = lorittaProfile.id.value
							it[cost] = 0
							it[background] = Background.findById(internalName)!!.id
							it[boughtAt] = System.currentTimeMillis()
						}
					} else if (it is LorittaBirthday2020.PremiumKeyReward) {
						DonationKeys.insert {
							it[userId] = user
							it[value] = 100.0
							it[expiresAt] = System.currentTimeMillis() + Constants.ONE_MONTH_IN_MILLISECONDS
							it[metadata] = jsonObject("type" to "LorittaBirthday2020Event")
						}
					}
				}
			}
		}
		return@task true
	}
}