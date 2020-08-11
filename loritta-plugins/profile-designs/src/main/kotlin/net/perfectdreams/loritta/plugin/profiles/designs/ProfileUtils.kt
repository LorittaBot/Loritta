package net.perfectdreams.loritta.plugin.profiles.designs

import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.Marriage
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.profile.ProfileUserInfoData
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.utils.CachedUserInfo
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object ProfileUtils {
	/**
	 * Gets the marriage information of the [userProfile]
	 *
	 * If the profile does not have an associated marriage, this will return null.
	 *
	 * @param  userProfile the user's profile
	 * @return the marriage information or null if the user isn't married
	 */
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

	/**
	 * Gets how many reputations the [userInfo] has
	 *
	 * @param  userInfo the user's information
	 * @return the reputation count
	 */
	fun getReputationCount(userInfo: ProfileUserInfoData) = transaction(Databases.loritta) {
		Reputations.select { Reputations.receivedById eq userInfo.id }.count()
	}

	/**
	 * Gets the user's global position in the experience ranking
	 *
	 * @param  userProfile the user's profile
	 * @return the user's current global position in the experience ranking
	 */
	fun getGlobalExperiencePosition(userProfile: Profile) = transaction(Databases.loritta) {
		Profiles.select { Profiles.xp greaterEq userProfile.xp }.count()
	}

	/**
	 * Gets the user's global position in the economy ranking
	 *
	 * @param  userProfile the user's profile
	 * @return the user's current global position in the economy ranking
	 */
	fun getGlobalEconomyPosition(userProfile: Profile) = transaction(Databases.loritta) {
		Profiles.select { Profiles.money greaterEq userProfile.money }.count()
	}

	/**
	 * Gets the user's local profile in the [guild] or null if the user does not have a profile in the guild
	 *
	 * @param  guild    the guild that the profile will be retrieved from
	 * @param  userInfo the user's information
	 * @return the user's current local position in the experience ranking
	 */
	fun getLocalProfile(guild: Guild, userInfo: ProfileUserInfoData) = transaction(Databases.loritta) {
		GuildProfile.find { (GuildProfiles.guildId eq guild.idLong) and (GuildProfiles.userId eq userInfo.id) }.firstOrNull()
	}

	/**
	 * Gets the user's local position in the experience ranking
	 *
	 * @param  localProfile the user's local profile
	 * @return the user's current local position in the experience ranking
	 */
	fun getLocalExperiencePosition(localProfile: GuildProfile?) = if (localProfile != null) {
		transaction(Databases.loritta) {
			GuildProfiles.select { (GuildProfiles.guildId eq localProfile.guildId) and (GuildProfiles.xp greaterEq localProfile.xp) }.count()
		}
	} else {
		null
	}

	data class MarriageInfo(
			val marriage: Marriage,
			val partner: CachedUserInfo
	)
}