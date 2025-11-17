package net.perfectdreams.loritta.morenitta.blackjack

import java.security.SecureRandom

class Blackjack(val random: SecureRandom) {
    // A simple blackjack implementation :3
    val dealerHand = Hand()
    var activePlayerHand = Hand()
    val playerHands = mutableListOf(activePlayerHand)

    // Hit the decks!
    // https://youtu.be/WbV8briBm5s
    val deck = mutableListOf<Card>()

    var gameState: GameState = GameState.PlayerTurn
        private set

    var paidInsurance = false
        private set

    val standRules = DealerSoftRules.STAND_ON_SOFT_17

    init {
        for (rank in Card.Rank.entries) {
            for (suit in Card.Suit.entries) {
                deck.add(Card(suit, rank))
            }
        }
        // Shuffle it!
        deck.shuffle(random)

        // TODO: remove this!
        // Test Blackjacks:
        // dealerHand.addCard(Card(Card.Suit.HEARTS, Card.Rank.ACE), true)
        // dealerHand.addCard(Card(Card.Suit.HEARTS, Card.Rank.QUEEN), false)

        // activePlayerHand.addCard(Card(Card.Suit.HEARTS, Card.Rank.ACE), true)
        // activePlayerHand.addCard(Card(Card.Suit.HEARTS, Card.Rank.QUEEN), true)

        // Test Splits:
        // activePlayerHand.addCard(Card(Card.Suit.HEARTS, Card.Rank.THREE), true)
        // activePlayerHand.addCard(Card(Card.Suit.HEARTS, Card.Rank.THREE), true)

        dealerHand.addCard(deck.removeFirst(), true)
        dealerHand.addCard(deck.removeFirst(), false)

        activePlayerHand.addCard(deck.removeFirst(), true)
        activePlayerHand.addCard(deck.removeFirst(), true)

        if (activePlayerHand.hasNaturalBlackjack()) {
            // If the player has a natural blackjack, we'll start the dealer round right away!
            startDealerRound()
            endDealerRound()
        }
    }

    fun hit() {
        this.activePlayerHand.addCard(this.deck.removeFirst(), true)
    }

    fun stand() {
        this.activePlayerHand.setStanding()
    }

    fun doubleDown() {
        // We hit and stand as is...
        hit()
        stand()

        // And we mark it as a doubled down hand!
        this.activePlayerHand.wasDoubledDown = true
    }

    fun startDealerRound() {
        this.gameState = GameState.DealerTurn
        this.dealerHand.cards[1].isFaceUp = true
    }

    fun endDealerRound() {
        this.gameState = GameState.GameOver
    }

    fun noAvailableHands() {
        this.gameState = GameState.GameOver
    }

    fun setInsuranceAsPaid() {
        this.paidInsurance = true
    }

    fun executeDealerRound(): DealerLogicResult {
        require(this.gameState is GameState.DealerTurn) { "You must start a dealer round before executing it!" }

        val dealerHand = this.dealerHand.calculateScore(true)

        val validScore = dealerHand.score
        val isSoft17 = dealerHand.maxScore == 17 && dealerHand.minScore != 17

        when (standRules) {
            DealerSoftRules.STAND_ON_SOFT_17 -> {
                // Dealer stands on soft 17
                if (validScore >= 17) {
                    // Dealer must stand on 17 or more!
                    return DealerLogicResult.Stop
                } else {
                    this.dealerHand.addCard(this.deck.removeFirst(), true)
                    return DealerLogicResult.Continue
                }
            }
            DealerSoftRules.HIT_ON_SOFT_17 -> {
                // Dealer hits on soft 17, stands on hard 17+
                if (validScore > 17 || (validScore == 17 && !isSoft17)) {
                    // Dealer must stand on 17 or more!
                    return DealerLogicResult.Stop
                } else {
                    this.dealerHand.addCard(this.deck.removeFirst(), true)
                    return DealerLogicResult.Continue
                }
            }
        }
    }

    fun getHandStateForAllHands(): Map<Hand, HandState> {
        return this.playerHands.associateWith { getHandStateForHand(it) }
    }

    fun getHandStateForHand(hand: Hand): HandState {
        val playerScore = hand.calculateScore(true)
        val dealerScore = this.dealerHand.calculateScore(true)

        val isGameOver = this.gameState is GameState.GameOver

        // Check for player bust first
        if (playerScore.hasBusted())
            return HandState.Busted

        // If game is still in progress, always show the hand as in progress
        if (!isGameOver)
            return HandState.InProgress

        // Hand automatically wins if the dealer score has busted
        if (dealerScore.hasBusted())
            return HandState.Win(false)

        if (isInsuranceAvailable()) {
            // The dealer MAY have a natural blackjack here! So we'll skip...
        } else {
            if (dealerHand.cards.size == 2 && hand.cards.size == 2) {
                val playerHasBlackjack = hand.hasNaturalBlackjack()
                val dealerHasBlackjack = dealerHand.hasNaturalBlackjack()

                if (playerHasBlackjack && dealerHasBlackjack)
                    return HandState.Push

                if (playerHasBlackjack)
                    return HandState.Win(true)

                if (dealerHasBlackjack)
                    return HandState.Lose(true)
            }
        }

        // Compare scores when both are done
        return when {
            playerScore.score > dealerScore.score -> HandState.Win(false)
            playerScore.score < dealerScore.score -> HandState.Lose(false)
            else -> HandState.Push
        }
    }

    fun isInsuranceAvailable(): Boolean {
        return !this.paidInsurance && this.playerHands.size == 1 && this.activePlayerHand.cards.size == 2 && this.dealerHand.cards.size == 2 && this.dealerHand.cards.first().rank == Card.Rank.ACE && this.dealerHand.cards.count { it.isFaceUp } == 1
    }

    fun isSplitAvailable(): Boolean {
        if (this.playerHands.size != 1)
            return false

        if (this.activePlayerHand.cards.size != 2)
            return false

        val card1 = this.activePlayerHand.cards[0]
        val card2 = this.activePlayerHand.cards[1]

        if (card1.rank != card2.rank)
            return false

        return true
    }

    fun splitHand() {
        require(this.isSplitAvailable()) { "Split is not available for this hand!" }

        val card1 = this.activePlayerHand.cards[0]
        val card2 = this.activePlayerHand.cards[1]

        this.activePlayerHand.cards.remove(card2)
        val hand = Hand()
        hand.addCard(card2, true)

        this.playerHands.add(hand)
    }

    fun isLastHand(): Boolean {
        return this.activePlayerHand == this.playerHands.last()
    }

    fun moveToNextHand() {
        require(!isLastHand()) { "Cannot move to the next hand because this is the last hand!" }

        val index = this.playerHands.indexOf(this.activePlayerHand)
        this.activePlayerHand = this.playerHands[index + 1]
    }

    /**
     * Processes the current hand state after a player action
     * Returns whenever the dealer round should start
     */
    fun processHandAfterAction(): HandProgressionResult {
        val currentScore = this.activePlayerHand.calculateScore(true)

        // Check if current hand busted
        if (currentScore.hasBusted()) {
            if (!isLastHand()) {
                moveToNextHand()
                return HandProgressionResult.MovedToNextHand
            } else {
                val allHandsBusted = playerHands.all { it.calculateScore(true).hasBusted() }
                if (allHandsBusted) {
                    noAvailableHands()
                    return HandProgressionResult.AllHandsBusted
                } else {
                    return HandProgressionResult.StartDealerRound
                }
            }
        }

        // Check if standing and should move to next hand
        if (this.activePlayerHand.isStanding) {
            if (!isLastHand()) {
                moveToNextHand()
                return HandProgressionResult.MovedToNextHand
            } else {
                return HandProgressionResult.StartDealerRound
            }
        }

        return HandProgressionResult.Continue
    }

    fun canDoubleDown(): Boolean {
        return (this.playerHands.size == 1 && this.activePlayerHand.cards.size == 2) || (this.playerHands.size == 2 && this.activePlayerHand.cards.size == 1)
    }

    sealed class DealerLogicResult {
        data object Stop : DealerLogicResult()
        data object Continue : DealerLogicResult()
    }

    sealed class GameState {
        data object PlayerTurn : GameState()
        data object DealerTurn : GameState()
        data object GameOver : GameState()
    }

    sealed class HandState {
        data object InProgress : HandState()
        data object Busted : HandState()
        data class Win(val isNaturalBlackjack: Boolean) : HandState()
        data class Lose(val isNaturalBlackjack: Boolean) : HandState()
        data object Push : HandState()
    }

    sealed class HandProgressionResult {
        data object Continue : HandProgressionResult()
        data object MovedToNextHand : HandProgressionResult()
        data object StartDealerRound : HandProgressionResult()
        data object AllHandsBusted : HandProgressionResult()
    }

    enum class DealerSoftRules {
        STAND_ON_SOFT_17,
        HIT_ON_SOFT_17
    }
}