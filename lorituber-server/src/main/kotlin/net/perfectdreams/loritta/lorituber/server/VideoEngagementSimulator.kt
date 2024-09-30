package net.perfectdreams.loritta.lorituber.server

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentVibes
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberVideoData
import java.security.SecureRandom

@Serializable
data class LoriTuberSuperViewerData(
    var preferredVideoContentCategories: List<LoriTuberVideoContentCategory>,
    var preferredVibes: LoriTuberVibes,
    var allocatedEngagement: Int
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
        999, // 999,
        999, // 999,
        20, // 999, // 999, // gameState.random.nextInt(0, 1000),
        // LoriTuberVibes(gameState.random.nextLong(0, 128)),
        LoriTuberVibes(0),
        0,
        0,
        0,
        mapOf()
    )

    val bools = listOf(false, true)

    val allPossibleCategoryCombinations = LoriTuberVideoContentCategory.entries.combinations(3)

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

    println(superViewers.size)

    val random = SecureRandom.getInstance("SHA1PRNG")

    var targetViews = 0
    var targetSuperviewerLikes = 0
    // var targetLikes = 0
    var targetDislikes = 0

    // The scaled thumbnail progress helps us actually setup that low thumbnail scores will have less views
    val thumbnailScoreProgress = video.thumbnailScore / 999.0
    // println("Progress: $thumbnailScoreProgress")
    val thumbnailScoreScaled = (video.thumbnailScore * thumbnailScoreProgress)
    // println("Thumbnail Score Scaled: $thumbnailScoreScaled")

    val videoScoreAverage = (video.recordingScore + video.editingScore) / 2

    superViewerLoop@ for (superViewer in superViewers) {
        // Is this a category that I'm familiar with?
        if (video.contentCategory !in superViewer.preferredVideoContentCategories)
            continue // Nope, bail out!

        // Do you match my vibes?
        // To makes things easier, we don't really need let people that wouldn't *vibe* the video to *see* the video
        // This somewhat simulates how YouTube attempts to only give you videos that you would like to see
        if (superViewer.preferredVibes.vibes != video.vibes.vibes)
            continue // You don't match my vibes...

        var superViewerLikedTheVideo = false

        repeat(superViewer.allocatedEngagement) {
            val shouldISeeThisVideoBasedOnThumbnail = random.nextDouble(0.0, 1000.0)

            if (thumbnailScoreScaled >= shouldISeeThisVideoBasedOnThumbnail) {
                // Throttle if the video is not up to snuff
                val targetViewsRelativeToLength = when (video.contentLength) {
                    LoriTuberContentLength.SHORT -> targetViews / 7
                    LoriTuberContentLength.MEDIUM -> targetViews / 3
                    LoriTuberContentLength.LONG -> targetViews
                }

                val softViewCapForVideoScoreAverage = when {
                    videoScoreAverage in 0 until 100 -> 250
                    videoScoreAverage in 100 until 200 -> 2_500
                    videoScoreAverage in 200 until 300 -> 10_000
                    else -> Int.MAX_VALUE // No cap!
                }

                if (false && targetViewsRelativeToLength > softViewCapForVideoScoreAverage) {
                    println("Throttled!")
                    val shouldActuallySeeThis = random.nextInt(0, 100)
                    if (shouldActuallySeeThis != 0)
                        return@repeat
                }

                val newViews = when (video.contentLength) {
                    LoriTuberContentLength.SHORT -> {
                        // targetViews += 10
                        7
                    }

                    LoriTuberContentLength.MEDIUM -> {
                        // targetViews += 10
                        3
                    }

                    LoriTuberContentLength.LONG -> {
                        // targetViews += 10
                        1
                    }
                }

                targetViews += newViews

                /* repeat(newViews) {
                    val myFancyScore = random.nextInt(0, 1000)
                    val likeThrottle = random.nextFloat()

                    // The more views you have, you end up gaining less likes (for some reason)
                    val targetLikeRatio = if (2_500 > targetViews) {
                        0.15f
                    } else if (50_000 > targetViews) {
                        0.12f
                    } else if (100_000 > targetViews) {
                        0.08f
                    } else if (500_000 > targetViews) {
                        0.065f
                    } else 0.035f // Anything else then we use the 3.5% average ratio

                    // YouTube videos have around ~3.5% view to like ratio
                    if (targetLikeRatio >= likeThrottle) {
                        val didILikeTheVideo = videoScoreAverage >= myFancyScore

                        if (didILikeTheVideo) {
                            targetLikes++
                            if (!superViewerLikedTheVideo)
                                targetSuperviewerLikes++
                            superViewerLikedTheVideo = true
                        }
                    }
                } */
            }
        }
    }

    // Fuck this shit
    val likeThrottle = random.nextFloat()

    // The more views you have, you end up gaining less likes (for some reason)
    val targetLikeRatio = if (2_500 > targetViews) {
        0.15f
    } else if (50_000 > targetViews) {
        0.12f
    } else if (100_000 > targetViews) {
        0.08f
    } else if (500_000 > targetViews) {
        0.065f
    } else 0.035f // Anything else then we use the 3.5% average ratio

    val targetLikes = targetViews * targetLikeRatio
    println("Target views: $targetViews")
    println("Target likes: ${targetLikes.toInt()}")
    println("Ratio: ${(targetLikes / targetViews.toDouble()) * 100}")
    println("Target superviewer likes: $targetSuperviewerLikes")
    println("Target dislikes: $targetDislikes")

// TODO: Depending on how many views we have as a target, make the "long tail" smaller to give faster feedback to the user
}

fun easeInSine(x: Double): Double {
    return 1 - Math.cos((x * Math.PI) / 2)
}
