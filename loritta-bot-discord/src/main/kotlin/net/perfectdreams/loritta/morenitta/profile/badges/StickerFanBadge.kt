package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsFinishedAlbumUsers
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class StickerFanBadge(val pudding: Pudding) : Badge.LorittaBadge(
	UUID.fromString("d5e6b7be-5bd4-4822-b81e-1285427f208f"),
	ProfileDesignManager.I18N_BADGES_PREFIX.StickerFan.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.StickerFan.TitlePlural,
	ProfileDesignManager.I18N_BADGES_PREFIX.StickerFan.Description,
	"sticker_fan.png",
	LorittaEmojis.StickerFan,
	100
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return pudding.transaction {
			// Get the latest finished album
			val latestEvents = LoriCoolCardsEvents.select(LoriCoolCardsEvents.id)
				.orderBy(LoriCoolCardsEvents.endsAt, SortOrder.DESC)
                // We get the last two events
				.limit(2)
				.toList()

            if (latestEvents.isEmpty())
                return@transaction false // There hasn't had any cool cards event yet...

			// Have we finished any of those events?
			LoriCoolCardsFinishedAlbumUsers.selectAll().where { LoriCoolCardsFinishedAlbumUsers.user eq user.id and (LoriCoolCardsFinishedAlbumUsers.event inList latestEvents.map { it[LoriCoolCardsEvents.id] }) }
				.count() >= 1L
		}
	}
}
