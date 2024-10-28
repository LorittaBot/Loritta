package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.CraftedReactionEventItems
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.ReactionEventPlayers
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEvent
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEventReward
import net.perfectdreams.loritta.morenitta.reactionevents.events.Halloween2024ReactionEvent
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.*

sealed class ReactionEventBadge(
	val pudding: Pudding,
	id: UUID,
	title: StringI18nData,
	description: StringI18nData,
	badgeName: String,
	priority: Int,
	reactionEvent: ReactionEvent
) : Badge.LorittaBadge(
	id,
	title,
	description,
	badgeName,
	priority
) {
	class Halloween2024ReactionEventBadge(pudding: Pudding) : ReactionEventBadge(
		pudding,
		UUID.fromString("e9de17d4-8ee6-4f18-b8a0-ef9087b5ec43"),
		ProfileDesignManager.I18N_BADGES_PREFIX.Halloween2024.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.Halloween2024.Description,
		"halloween2019.png",
		100,
		Halloween2024ReactionEvent
	)

	private val eventInternalId = reactionEvent.internalId
	private val requiredPoints = reactionEvent.rewards.filterIsInstance<ReactionEventReward.BadgeReward>().first().requiredPoints

	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return pudding.transaction {
			CraftedReactionEventItems.innerJoin(ReactionEventPlayers).selectAll()
				.where {
					CraftedReactionEventItems.event eq eventInternalId and (ReactionEventPlayers.userId eq user.id)
				}
				.count() >= requiredPoints
		}
	}
}