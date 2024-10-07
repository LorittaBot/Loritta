package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberVideoSimulatedEngagements : LongIdTable() {
    val video = reference("video", LoriTuberVideos).index()

    val easingType = text("easing_type")
    val engagementStartTick = long("engagement_start_tick")
    val engagementEndTick = long("engagement_end_tick")

    val startViews = integer("start_views")
    val startLikes = integer("start_likes")
    val startDislikes = integer("start_dislikes")
    val targetViews = integer("target_views")
    val targetLikes = integer("target_likes")
    val targetDislikes = integer("target_dislikes")
}