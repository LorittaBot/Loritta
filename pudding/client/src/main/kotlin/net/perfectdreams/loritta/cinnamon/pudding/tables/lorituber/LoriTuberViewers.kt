package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberViewers : LongIdTable() {
    val handle = text("handle").index()
    val activityStartTicks = long("activity_start_ticks").index()
    val activityEndTicks = long("activity_end_ticks").index()
    val watchingVideo = reference("watching_video", LoriTuberVideos).nullable().index()
    val watchCooldownTicks = long("watch_cooldown_ticks").nullable()

    // This is a big fat bitset for OPTIMIZATION PURPOSES
    // Because this is WAY faster than the previous solution of "let's store it in separate tables" or "let's store as jsonb"
    val vibesCategory1 = long("vibes_category1").nullable()
    val vibesCategory2 = long("vibes_category2").nullable()
    val vibesCategory3 = long("vibes_category3").nullable()
    val vibesCategory4 = long("vibes_category4").nullable()
    val vibesCategory5 = long("vibes_category5").nullable()
    val vibesCategory6 = long("vibes_category6").nullable()
    val vibesCategory7 = long("vibes_category7").nullable()
    val vibesCategory8 = long("vibes_category8").nullable()
    val vibesCategory9 = long("vibes_category9").nullable()
    val vibesCategory10 = long("vibes_category10").nullable()
}