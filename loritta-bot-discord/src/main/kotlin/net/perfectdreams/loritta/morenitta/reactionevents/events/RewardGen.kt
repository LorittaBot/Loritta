package net.perfectdreams.loritta.morenitta.reactionevents.events

import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEventReward

fun main() {
    val rewards = listOf(
        ReactionEventReward.SonhosReward(100, false, 12500),
        ReactionEventReward.SonhosReward(150, false, 50000),
        ReactionEventReward.SonhosReward(200, false, 82500),
        ReactionEventReward.SonhosReward(250, false, 137500),
        ReactionEventReward.SonhosReward(300, false, 187500),
        ReactionEventReward.SonhosReward(350, false, 250000),
        ReactionEventReward.SonhosReward(400, false, 312500),
        ReactionEventReward.SonhosReward(450, false, 387500),
        ReactionEventReward.SonhosReward(500, false, 480000),
        ReactionEventReward.SonhosReward(550, false, 600000),
        ReactionEventReward.SonhosReward(600, false, 700000),
        ReactionEventReward.SonhosReward(650, false, 800000),
        ReactionEventReward.SonhosReward(700, false, 1000000),
    )

    println(rewards.sumOf { it.sonhos })
}