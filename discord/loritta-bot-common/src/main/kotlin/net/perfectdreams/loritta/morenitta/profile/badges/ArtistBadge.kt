package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.utils.config.FanArtArtist

class ArtistBadge(val loritta: LorittaBot) : Badge("artist_badge.png", 25) {
    override suspend fun checkIfUserDeservesBadge(
        user: ProfileUserInfoData,
        profile: Profile,
        mutualGuilds: Set<Long>
    ): Boolean {
        return loritta.fanArtArtists.any {
            it.socialNetworks?.filterIsInstance<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
                ?.firstOrNull()?.id == user.id.toString()
        }
    }
}