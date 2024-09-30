package net.perfectdreams.loritta.lorituber.server

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentVibes
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberVideoData
import java.security.SecureRandom
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
data class LoriTuberSimpleSuperViewerData(
    var preferredVibes: LoriTuberVibes
)

fun main() {
    // If a video is PERFECT, how many views should it get on a 100% downtrend?
    // = 100k views
    // That means that our MINIMUM algo boost should ALWAYS be 10x
    //
    // 10x really seems to be the "sweet spot"
    //
    // But how can we figure out WHAT'S the target view if the user has a BAD video? This is WAY harder to figure out
    // ^ TODO: This is the hard part right now, we need to figure out a way to "nerf" bad videos somehow while rewarding good videos

    val video = LoriTuberVideoData(
        0,
        true,
        0,
        LoriTuberVideoContentCategory.GAMES,
        LoriTuberContentLength.MEDIUM,
        20, // 999,
        20, // 999,
        20, // 999, // 999, // gameState.random.nextInt(0, 1000),
        // LoriTuberVibes(gameState.random.nextLong(0, 128)),
        LoriTuberVibes(0),
        0,
        0,
        0,
        mapOf()
    )

    val bools = listOf(false, true)

    val vibers = mutableMapOf<LoriTuberVideoContentCategory, LoriTuberSimpleSuperViewerData>()

    for (category in LoriTuberVideoContentCategory.entries) {
        val vibes = LoriTuberVibes(0)
        vibes.setVibe(LoriTuberVideoContentVibes.VIBE1, bools.random())
        vibes.setVibe(LoriTuberVideoContentVibes.VIBE2, bools.random())
        vibes.setVibe(LoriTuberVideoContentVibes.VIBE3, bools.random())
        vibes.setVibe(LoriTuberVideoContentVibes.VIBE4, bools.random())
        vibes.setVibe(LoriTuberVideoContentVibes.VIBE5, bools.random())
        vibes.setVibe(LoriTuberVideoContentVibes.VIBE6, bools.random())
        vibes.setVibe(LoriTuberVideoContentVibes.VIBE7, bools.random())

        vibers[category] = LoriTuberSimpleSuperViewerData(LoriTuberVibes(vibes.vibes))
    }

    /* val allPossibleCategoryCombinations = LoriTuberVideoContentCategory.entries.combinations(3)

    val superViewers = mutableListOf<LoriTuberSuperViewerData>()

    for (categories in allPossibleCategoryCombinations) {
        for (vibe1 in bools) {
            for (vibe2 in bools) {
                for (vibe3 in bools) {
                    for (vibe4 in bools) {
                        for (vibe5 in bools) {
                            for (vibe6 in bools) {
                                for (vibe7 in bools) {
                                    val vibes = LoriTuberVibes(0)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE1, vibe1)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE2, vibe2)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE3, vibe3)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE4, vibe4)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE5, vibe5)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE6, vibe6)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE7, vibe7)

                                    superViewers.add(
                                        LoriTuberSuperViewerData(
                                            categories,
                                            vibes,
                                            // 1000 allocated engagement gives ~1 million views
                                            // 1000 allocated engagement gives ~1 million views (omg)
                                            // when the thumbnail + editing + recording = max

                                            // TODO: It seems that 10 allocated engagement is a "healthy" default instead of 1
                                            5_000, // 1000 // 1, // gameState.random.nextInt(1, 10)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    println(superViewers.size) */

    val random = SecureRandom.getInstance("SHA1PRNG")

    // The scaled thumbnail progress helps us actually setup that low thumbnail scores will have less views

    val vibesOfTheCurrentCategory = vibers[video.contentCategory]!!
    val videoGameplayScore = (video.recordingScore + video.editingScore + video.thumbnailScore) / 3
    val videoGameplayProgress = videoGameplayScore / 999.0

    println("Video score: $videoGameplayScore")
    println("Progress: $videoGameplayProgress")
    // println("Scaled: $videoScoreScaled")
    println(vibesOfTheCurrentCategory)

    var matchedVibes = 0
    for (vibe in LoriTuberVideoContentVibes.entries) {
        if (vibesOfTheCurrentCategory.preferredVibes.vibeType(vibe) == video.vibes.vibeType(vibe)) {
            matchedVibes++
        }
    }
    println("Matched vibes: $matchedVibes")

    val baseViews = when (matchedVibes) {
        7 -> 1_000_000
        6 -> 750_000
        5 -> 500_000
        4 -> 250_000
        3 -> 100_000
        2 -> 100_000
        1 -> 100_000
        0 -> 100_000
        else -> error("Unsupported matched vibes count $matchedVibes")
    }

    println("Base views: $baseViews")

    // The target views should change depending on the current algo boost of the category + vibes
    val targetViews = baseViews * easeInQuad(videoGameplayProgress)
    val targetViewsAsInt = targetViews.toInt()

    var targetLikes = 0
    var targetDislikes = 0
    repeat(targetViewsAsInt) {
        val shouldILike = random.nextFloat()

        // Yeah, the likes and dislikes are just cosmetic
        if (0.03f >= shouldILike)
            targetLikes++
        else if (0.0312f >= shouldILike)
            targetDislikes++
    }

    // val targetLikesRatio = getTargetLikesRatio(targetViewsAsInt)
    // println(targetLikesRatio)

    // var targetLikes = (targetViews * targetLikesRatio).toInt()

    println(targetViewsAsInt)
    println(targetLikes)
    println(targetDislikes)
}

fun easeInQuad(x: Double): Double {
    return x * x
}

fun easeInCubic(x: Double): Double {
    return x * x * x
}

fun easeInQuart(x: Double): Double {
    return x * x * x * x
}

fun easeInCirc(x: Double): Double {
    return 1 - sqrt(1 - x.pow(2))
}