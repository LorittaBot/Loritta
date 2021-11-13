package net.perfectdreams.loritta.plugin.lorittabirthday2020.badges

import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Players
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.CollectedBirthday2020Points
import net.perfectdreams.loritta.plugin.lorittabirthday2020.utils.BirthdayTeam
import net.perfectdreams.loritta.profile.Badge
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GabrielaBadge : Badge("badges/birthday2020_gabriela.png", 100) {
	override fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		val playerResult = transaction(Databases.loritta) {
			Birthday2020Players.select { Birthday2020Players.user eq profile.id }
					.firstOrNull()
		} ?: return false

		if (playerResult[Birthday2020Players.team] != BirthdayTeam.GABRIELA)
			return false

		val count = transaction(Databases.loritta) {
			CollectedBirthday2020Points.select {
				CollectedBirthday2020Points.user eq user.idLong
			}.count()
		}

		return count >= 100
	}
}