package net.perfectdreams.loritta.profile

import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.utils.config.FanArtArtist
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

class ArtistBadge : Badge("artist_badge.png", 25) {
	override fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: List<JsonElement>): Boolean {
		return loritta.fanArtArtists.any { it.socialNetworks?.firstIsInstanceOrNull<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()?.id == user.id }
	}
}