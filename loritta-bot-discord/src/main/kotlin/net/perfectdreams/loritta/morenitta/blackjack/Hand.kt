package net.perfectdreams.loritta.morenitta.blackjack

class Hand {
    val cards = mutableListOf<Card>()
    var isStanding = false
        private set
    var wasDoubledDown = false

    fun setStanding() {
        this.isStanding = true
    }

    fun addCard(card: Card, faceUp: Boolean) {
        cards.add(card)
        card.isFaceUp = faceUp
    }

    fun calculateScore(onlyFaceUp: Boolean): HandScore {
        var minScore = 0
        var maxScore = 0
        var aceCount = 0

        val validCards = if (onlyFaceUp) {
            this.cards.filter { it.isFaceUp }
        } else { this.cards }

        for (card in validCards) {
            minScore += when (card.rank) {
                Card.Rank.ACE -> 1
                Card.Rank.TWO -> 2
                Card.Rank.THREE -> 3
                Card.Rank.FOUR -> 4
                Card.Rank.FIVE -> 5
                Card.Rank.SIX -> 6
                Card.Rank.SEVEN -> 7
                Card.Rank.EIGHT -> 8
                Card.Rank.NINE -> 9
                Card.Rank.TEN -> 10
                Card.Rank.JACK -> 10
                Card.Rank.QUEEN -> 10
                Card.Rank.KING -> 10
            }

            maxScore += when (card.rank) {
                Card.Rank.ACE -> {
                    // Only the FIRST ace is considered!
                    aceCount++
                    if (aceCount == 1)
                        11
                    else
                        1
                }
                Card.Rank.TWO -> 2
                Card.Rank.THREE -> 3
                Card.Rank.FOUR -> 4
                Card.Rank.FIVE -> 5
                Card.Rank.SIX -> 6
                Card.Rank.SEVEN -> 7
                Card.Rank.EIGHT -> 8
                Card.Rank.NINE -> 9
                Card.Rank.TEN -> 10
                Card.Rank.JACK -> 10
                Card.Rank.QUEEN -> 10
                Card.Rank.KING -> 10
            }
        }

        return HandScore(minScore, maxScore)
    }

    fun hasNaturalBlackjack(): Boolean {
        if (cards.size != 2)
            return false

        val hasAce = cards.any { it.rank == Card.Rank.ACE }
        val hasAnyTenValue = cards.any { it.rank == Card.Rank.TEN || it.rank == Card.Rank.JACK || it.rank == Card.Rank.QUEEN || it.rank == Card.Rank.KING }
        return hasAce && hasAnyTenValue
    }

    class HandScore(val minScore: Int, val maxScore: Int) {
        val score get() = if (21 >= maxScore) maxScore else minScore

        fun hasBusted() = score > 21
    }
}