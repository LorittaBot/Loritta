package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingMarriage
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.serializable.Marriage
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class MarriagesService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getMarriageByUser(user: UserId): PuddingMarriage? {
        val userId = user.value.toLong()

        return pudding.transaction {
            val selfMarriage = MarriageParticipants
                .innerJoin(UserMarriages)
                .selectAll()
                .where {
                    UserMarriages.active eq true and (MarriageParticipants.user eq userId)
                }
                .firstOrNull()

            if (selfMarriage == null)
                return@transaction null

            val marriageParticipants = MarriageParticipants.selectAll()
                .where {
                    MarriageParticipants.marriage eq selfMarriage[UserMarriages.id]
                }
                .toList()

            return@transaction PuddingMarriage(
                pudding,
                Marriage(
                    selfMarriage[UserMarriages.id].value,
                    marriageParticipants.map { UserId(it[MarriageParticipants.user]) },
                    selfMarriage[UserMarriages.createdAt].toKotlinInstant(),
                    selfMarriage[UserMarriages.coupleName],
                    selfMarriage[UserMarriages.coupleBadge].toString(),
                    0,
                    selfMarriage[UserMarriages.hugCount],
                    selfMarriage[UserMarriages.kissCount],
                    selfMarriage[UserMarriages.headPatCount]
                )
            )
        }
    }
}