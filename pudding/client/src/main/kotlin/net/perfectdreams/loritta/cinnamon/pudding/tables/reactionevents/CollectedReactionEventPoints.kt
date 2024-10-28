package net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object CollectedReactionEventPoints : LongIdTable() {
    val user = reference("user", ReactionEventPlayers).index()
    val drop = reference("drop", ReactionEventDrops).index()
    val points = integer("points")
    val collectedAt = timestampWithTimeZone("collected_at")
    val valid = bool("valid")
    val associatedWithCraft = optReference("associated_with_craft", CraftedReactionEventItems)
}