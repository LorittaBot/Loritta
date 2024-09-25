package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentCategory
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberViewerVideoPreferences : LongIdTable() {
    val viewer = reference("viewer", LoriTuberViewers)
    val category = enumerationByName("category", 64, LoriTuberVideoContentCategory::class).index()

    // "Follow trends" are used to change the user's likes and dislikes over time, by following what's trending the user end up changing their likes and dislikes over time
    // TODO: Maybe users should be influenced by what they watch? The more they enjoy a creator, more they will align to what the creator is putting out
    //  This way you can somewhat "shift" user opinions by creating content that people enjoy, and then slowly shifting your niche
    //  Users should be created from these shifted opinions tho

    // TODO: We should also store script/record/editing/thumbnail viewer requirements

    // Dislikes <---> Likes
    // For consistency, even tho the user only dislikes -> neutral -> likes the category, the values are...
    // -5
    // 0
    // 5
    // val categoryFollowTrendRatio = integer("category_follow_trend_ratio")
    val categoryPreference = integer("category_preference")

    // val vibe1FollowTrendRatio = integer("vibe1_follow_trend_ratio")
    val vibe1Preference = integer("vibe1_preference")

    // val vibe2FollowTrendRatio = integer("vibe2_follow_trend_ratio")
    val vibe2Preference = integer("vibe2_preference")

    // val vibe3FollowTrendRatio = integer("vibe3_follow_trend_ratio")
    val vibe3Preference = integer("vibe3_preference")

    // val vibe4FollowTrendRatio = integer("vibe4_follow_trend_ratio")
    val vibe4Preference = integer("vibe4_preference")

    // val vibe5FollowTrendRatio = integer("vibe5_follow_trend_ratio")
    val vibe5Preference = integer("vibe5_preference")

    // val vibe6FollowTrendRatio = integer("vibe6_follow_trend_ratio")
    val vibe6Preference = integer("vibe6_preference")

    // val vibe7FollowTrendRatio = integer("vibe7_follow_trend_ratio")
    val vibe7Preference = integer("vibe7_preference")
}