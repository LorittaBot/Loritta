package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.blackjack

import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.serialization.Serializable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.blackjack.Card
import net.perfectdreams.loritta.morenitta.blackjack.Hand
import kotlin.time.Duration.Companion.minutes

object BlackjackUtils {
    /**
     * The delay between dealer actions
     */
    const val DEALER_ACTION_DELAY = 1_000L
    val AUTO_STAND_DELAY = 5.minutes
    const val MINIMUM_BET = 100L
    const val MAXIMUM_BET = 20_000_000L

    /**
     * Converts the current [card] state into emojis
     */
    fun createAsEmoji(loritta: LorittaBot, card: Card): String {
        if (!card.isFaceUp)
            return "${loritta.emojiManager.get(LorittaEmojis.CardUnknownLeft)}${loritta.emojiManager.get(LorittaEmojis.CardUnknownRight)}"

        val part1 = when (card.suit) {
            Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubsLeft)
            Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamondsLeft)
            Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHeartsLeft)
            Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpadesLeft)
        }

        val part2 = when (card.rank) {
            Card.Rank.ACE -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubsARight)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamondsARight)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHeartsARight)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpadesARight)
            }
            Card.Rank.TWO -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubs2Right)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamonds2Right)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHearts2Right)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpades2Right)
            }
            Card.Rank.THREE -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubs3Right)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamonds3Right)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHearts3Right)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpades3Right)
            }
            Card.Rank.FOUR -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubs4Right)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamonds4Right)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHearts4Right)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpades4Right)
            }
            Card.Rank.FIVE -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubs5Right)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamonds5Right)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHearts5Right)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpades5Right)
            }
            Card.Rank.SIX -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubs6Right)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamonds6Right)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHearts6Right)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpades6Right)
            }
            Card.Rank.SEVEN -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubs7Right)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamonds7Right)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHearts7Right)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpades7Right)
            }
            Card.Rank.EIGHT -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubs8Right)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamonds8Right)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHearts8Right)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpades8Right)
            }
            Card.Rank.NINE -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubs9Right)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamonds9Right)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHearts9Right)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpades9Right)
            }
            Card.Rank.TEN -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubs10Right)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamonds10Right)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHearts10Right)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpades10Right)
            }
            Card.Rank.JACK -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubsJRight)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamondsJRight)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHeartsJRight)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpadesJRight)
            }
            Card.Rank.QUEEN -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubsQRight)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamondsQRight)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHeartsQRight)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpadesQRight)
            }
            Card.Rank.KING -> when (card.suit) {
                Card.Suit.CLUBS -> loritta.emojiManager.get(LorittaEmojis.CardClubsKRight)
                Card.Suit.DIAMONDS -> loritta.emojiManager.get(LorittaEmojis.CardDiamondsKRight)
                Card.Suit.HEARTS -> loritta.emojiManager.get(LorittaEmojis.CardHeartsKRight)
                Card.Suit.SPADES -> loritta.emojiManager.get(LorittaEmojis.CardSpadesKRight)
            }
        }

        return "$part1$part2"
    }

    fun InlineMessage<*>.createBlackjackTutorialMessage(
        loritta: LorittaBot,
        i18nContext: I18nContext,
    ) {
        this.useComponentsV2 = true

        val hand1Example = Hand()
        hand1Example.addCard(Card(Card.Suit.HEARTS, Card.Rank.TWO), true)
        hand1Example.addCard(Card(Card.Suit.SPADES, Card.Rank.JACK), true)

        val hand2Example = Hand()
        hand2Example.addCard(Card(Card.Suit.HEARTS, Card.Rank.ACE), true)
        hand2Example.addCard(Card(Card.Suit.SPADES, Card.Rank.KING), true)

        val hand3Example = Hand()
        hand3Example.addCard(Card(Card.Suit.CLUBS, Card.Rank.JACK), true)
        hand3Example.addCard(Card(Card.Suit.SPADES, Card.Rank.ACE), true)
        hand3Example.addCard(Card(Card.Suit.HEARTS, Card.Rank.SEVEN), true)

        val suits = "${loritta.emojiManager.get(LorittaEmojis.CardSpades)}${loritta.emojiManager.get(LorittaEmojis.CardClubs)}${loritta.emojiManager.get(LorittaEmojis.CardHearts)}${loritta.emojiManager.get(LorittaEmojis.CardDiamonds)}"
        val unknownCard = "${loritta.emojiManager.get(LorittaEmojis.CardUnknownLeft)}${loritta.emojiManager.get(LorittaEmojis.CardUnknownRight)}"
        container {
            text(
                buildString {
                    appendLine("# ${loritta.emojiManager.get(LorittaEmojis.CardSpades)} ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Title)}")
                    appendLine("## \uD83C\uDFB4 ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.WhatIsBlackjack.Title)}")
                    appendLine(i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.WhatIsBlackjack.Description))
                    appendLine()
                    appendLine(i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.WhatIsBlackjack.Objective))

                    appendLine("## \uD83C\uDCCF ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Deck.Title)}")
                    appendLine(i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Deck.Description))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Deck.Cards2to10))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Deck.CardsJQK))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Deck.Ace))
                    appendLine("  * " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Deck.AceExplanation))
                    appendLine("  * " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Deck.HandScore))
                    appendLine()
                    appendLine(i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Deck.SuitsWarning(suits)))

                    appendLine("## \uD83D\uDCCA ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.ScoreExamples.Title)}")
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.ScoreExamples.Example1(hand1Example.cards.joinToString(" ") { createAsEmoji(loritta, it) })))
                    appendLine("  * " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.ScoreExamples.Example1Explanation))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.ScoreExamples.Example2(hand2Example.cards.joinToString(" ") { createAsEmoji(loritta, it) })))
                    appendLine("  * " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.ScoreExamples.Example2Explanation1))
                    appendLine("  * " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.ScoreExamples.Example2Explanation2))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.ScoreExamples.Example3(hand3Example.cards.joinToString(" ") { createAsEmoji(loritta, it) })))
                    appendLine("  * " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.ScoreExamples.Example3Explanation1))
                    appendLine("  * " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.ScoreExamples.Example3Explanation2))

                    appendLine("## \uD83C\uDFAF ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.HowToPlay.Title)}")
                    appendLine("1. ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.HowToPlay.Step1Title)}")
                    appendLine("  * ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.HowToPlay.Step1PlayerCards)}")
                    appendLine("  * ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.HowToPlay.Step1DealerCard(unknownCard))}")
                    appendLine("2. ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.HowToPlay.Step2)}")
                    appendLine("3. ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.HowToPlay.Step3)}")
                    appendLine("4. ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.HowToPlay.Step4)}")

                    appendLine("## \uD83C\uDFAE ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Actions.Title)}")
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Actions.Hit))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Actions.Stand))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Actions.DoubleDown))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Actions.Split))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Actions.Insurance))

                    appendLine("## \uD83C\uDFC6 ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Results.Title)}")
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Results.BlackjackNatural))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Results.NormalWin))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Results.Push))
                    appendLine("* " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Results.Loss))
                    appendLine("  * " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Tutorial.Results.LossNote))
                }
            )
        }
    }

    fun InlineMessage<*>.createBlackjackHouseRulesMessage(
        loritta: LorittaBot,
        i18nContext: I18nContext,
    ) {
        this.useComponentsV2 = true

        val hand1Example = Hand()
        hand1Example.addCard(Card(Card.Suit.HEARTS, Card.Rank.ACE), true)
        hand1Example.addCard(Card(Card.Suit.SPADES, Card.Rank.SIX), true)

        container {
            text(
                buildString {
                    appendLine("# ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.HouseRules.Title)}")
                    appendLine("* ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.HouseRules.DealerMustHit)}")
                    appendLine("* ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.HouseRules.DealerStandsSoft17(hand1Example.cards.joinToString("") { createAsEmoji(loritta, it) }))}")
                    appendLine("* ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.HouseRules.SingleDeck)}")
                    appendLine("* ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.HouseRules.DoubleAnyCard)}")
                    appendLine("* ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.HouseRules.DoubleAfterSplit)}")
                    appendLine("* ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.HouseRules.SplitOnce)}")
                    appendLine("* ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.HouseRules.HitSplitAces)}")
                    appendLine("* ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.HouseRules.NoSurrender)}")
                    appendLine("* ${i18nContext.get(I18nKeysData.Commands.Command.Blackjack.HouseRules.InsuranceAvailable)}")
                }
            )
        }
    }

    fun convertToSerializableStateHands(
        dealerHand: Hand,
        playerHands: List<Hand>
    ): StoredBlackjackHands {
        return StoredBlackjackHands(
            dealerHand.let {
                StoredBlackjackHands.StoredHand(
                    it.cards.map {
                        StoredBlackjackHands.StoredHand.StoredCard(
                            it.rank,
                            it.suit,
                            it.isFaceUp
                        )
                    },
                    it.wasDoubledDown
                )
            },
            playerHands.map {
                StoredBlackjackHands.StoredHand(
                    it.cards.map {
                        StoredBlackjackHands.StoredHand.StoredCard(
                            it.rank,
                            it.suit,
                            it.isFaceUp
                        )
                    },
                    it.wasDoubledDown
                )
            }
        )
    }

    @Serializable
    class StoredBlackjackHands(
        val dealerHand: StoredHand,
        val playerHands: List<StoredHand>
    ) {
        @Serializable
        class StoredHand(
            val cards: List<StoredCard>,
            val wasDoubledDown: Boolean
        ) {
            @Serializable
            class StoredCard(
                val rank: Card.Rank,
                val suit: Card.Suit,
                val isFaceUp: Boolean
            )
        }
    }
}