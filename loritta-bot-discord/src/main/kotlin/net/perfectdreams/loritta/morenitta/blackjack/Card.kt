package net.perfectdreams.loritta.morenitta.blackjack

class Card(val suit: Suit, val rank: Rank) {
    var isFaceUp = false

    enum class Suit {
        CLUBS,
        DIAMONDS,
        HEARTS,
        SPADES
    }

    enum class Rank {
        ACE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING
    }
}