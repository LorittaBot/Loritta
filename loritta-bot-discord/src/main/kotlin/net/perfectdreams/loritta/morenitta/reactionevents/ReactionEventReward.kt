package net.perfectdreams.loritta.morenitta.reactionevents

sealed class ReactionEventReward(val requiredPoints: Int, val prestige: Boolean) {
    class BadgeReward(requiredPoints: Int, prestige: Boolean) : ReactionEventReward(requiredPoints, prestige)
    class SonhosReward(requiredPoints: Int, prestige: Boolean, val sonhos: Long) : ReactionEventReward(requiredPoints, prestige)
}