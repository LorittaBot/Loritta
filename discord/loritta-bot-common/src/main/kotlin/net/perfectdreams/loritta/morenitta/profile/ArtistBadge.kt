package net.perfectdreams.loritta.morenitta.profile

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.config.FanArtArtist

class ArtistBadge(val loritta: LorittaBot) : Badge("artist_badge.png", 25) {
	override suspend fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return loritta.fanArtArtists.any { it.socialNetworks?.filterIsInstance<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()?.firstOrNull()?.id == user.id }
	}
}