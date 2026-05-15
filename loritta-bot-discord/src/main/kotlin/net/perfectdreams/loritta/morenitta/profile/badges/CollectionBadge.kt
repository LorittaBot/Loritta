package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.CollectionsManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import java.util.*

class CollectionBadge(
    val pudding: Pudding,
    id: UUID,
    title: StringI18nData,
    description: StringI18nData,
    badgeFileName: String,
    emoji: LorittaEmojiReference,
    priority: Int,
    val collectionId: String
) : Badge.LorittaBadge(id, title, null, description, badgeFileName, emoji, priority) {
    override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
        return CollectionsManager.hasCompletedCollection(pudding, user.id, collectionId)
    }
}
