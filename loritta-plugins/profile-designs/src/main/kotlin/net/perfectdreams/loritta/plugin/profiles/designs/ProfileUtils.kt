package net.perfectdreams.loritta.plugin.profiles.designs

import com.mrpowergamerbr.loritta.dao.Marriage
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.utils.CachedUserInfo
import org.jetbrains.exposed.sql.transactions.transaction

object ProfileUtils {
	fun getMarriageInfo(userProfile: Profile): MarriageInfo? {
		val marriage = transaction(Databases.loritta) { userProfile.marriage }

		if (marriage != null) {
			val marriedWithId = if (marriage.user1 == userProfile.id.value) {
				marriage.user2
			} else {
				marriage.user1
			}.toString()

			return runBlocking { lorittaShards.retrieveUserInfoById(marriedWithId.toLong()) }?.let {
				MarriageInfo(marriage, it)
			}
		}

		return null
	}

	data class MarriageInfo(
			val marriage: Marriage,
			val partner: CachedUserInfo
	)
}