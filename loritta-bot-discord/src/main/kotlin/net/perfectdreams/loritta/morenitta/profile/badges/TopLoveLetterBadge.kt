package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageLoveLetters
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class TopLoveLetterBadge(val pudding: Pudding) : Badge.LorittaBadge(
    UUID.fromString("5c029e28-95ec-479e-9570-1ad9dab32817"),
    ProfileDesignManager.I18N_BADGES_PREFIX.TopLoveLetter.Title,
    ProfileDesignManager.I18N_BADGES_PREFIX.TopLoveLetter.TitlePlural,
    ProfileDesignManager.I18N_BADGES_PREFIX.TopLoveLetter.Description,
    "top_love_letter.png",
    LorittaEmojis.TopLoveLetter,
    20
) {
    override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
        return pudding.transaction {
            // 1. Get which marriage has sent the most love letters (group by the marriage!)
            val count = MarriageLoveLetters.marriage.count()
            val topMarriage = MarriageLoveLetters.innerJoin(UserMarriages)
                .select(MarriageLoveLetters.marriage, count)
                .where {
                    UserMarriages.active eq true
                }
                .groupBy(MarriageLoveLetters.marriage)
                .orderBy(count to SortOrder.DESC)
                .limit(1)
                .firstOrNull() ?: return@transaction false

            val marriageId = topMarriage[MarriageLoveLetters.marriage]

            // 2. Get the participants of the marriage
            val participants = MarriageParticipants.selectAll()
                .where {
                    MarriageParticipants.marriage eq marriageId
                }
                .map { it[MarriageParticipants.user] }
                .toList()

            // 3. Is the user in the participant list? Then they should deserve the badge!
            participants.contains(user.id)
        }
    }
}
