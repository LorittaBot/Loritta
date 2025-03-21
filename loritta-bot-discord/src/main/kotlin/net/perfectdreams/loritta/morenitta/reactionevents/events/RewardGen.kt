package net.perfectdreams.loritta.morenitta.reactionevents.events

import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEventReward

fun main() {
    // target: 5000000
    val rewards = listOf(
        ReactionEventReward.SonhosReward(100, false, 50_000),
        ReactionEventReward.SonhosReward(150, false, 150_000),
        ReactionEventReward.SonhosReward(200, false, 300_000),
        ReactionEventReward.SonhosReward(250, false, 500_000),
        ReactionEventReward.SonhosReward(300, false, 600_000),
        ReactionEventReward.SonhosReward(350, false, 700_000),
        ReactionEventReward.SonhosReward(400, false, 800_000),
        ReactionEventReward.SonhosReward(450, false, 900_000),
        ReactionEventReward.SonhosReward(500, false, 1_000_000)
    )

    println(rewards.sumOf { it.sonhos })
}