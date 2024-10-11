package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.utils.config.FanArtArtist
import java.util.*

class ArtistBadge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("81788d4a-7e6c-415f-8832-d55573f8c40b"),
	ProfileDesignManager.I18N_BADGES_PREFIX.Artist.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.Artist.Description,
	"artist.png",
	25
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		val galleryOfDreamsDataResponse = loritta.cachedGalleryOfDreamsDataResponse ?: return false
		return galleryOfDreamsDataResponse.artists.any {
			it.socialConnections
				.filterIsInstance<DiscordSocialConnection>()
				.firstOrNull()
				?.id == user.id.toLong()
		}
	}
}