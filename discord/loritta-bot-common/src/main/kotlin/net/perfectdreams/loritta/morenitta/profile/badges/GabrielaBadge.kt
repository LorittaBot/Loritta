package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.tables.Birthday2020Players
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectedBirthday2020Points
import net.perfectdreams.loritta.cinnamon.pudding.utils.BirthdayTeam
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.select

class GabrielaBadge(val loritta: LorittaBot) : Badge("birthday2020_gabriela.png", 100) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		val playerResult = loritta.pudding.transaction {
			Birthday2020Players.select { Birthday2020Players.user eq profile.id }
					.firstOrNull()
		} ?: return false

		if (playerResult[Birthday2020Players.team] != BirthdayTeam.GABRIELA)
			return false

		val count = loritta.pudding.transaction {
			CollectedBirthday2020Points.select {
				CollectedBirthday2020Points.user eq user.id.toLong()
			}.count()
		}

		return count >= 100
	}
}