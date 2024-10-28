package net.perfectdreams.loritta.morenitta.reactionevents

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import java.time.Instant
import java.util.*
import kotlin.math.round
import kotlin.random.Random

abstract class ReactionEvent {
    abstract val internalId: String
    abstract val startsAt: Instant
    abstract val endsAt: Instant
    abstract val reactionSets: List<ReactionSet>
    abstract val rewards: List<ReactionEventReward>
    open val guildMemberThreshold = 1_000

    abstract fun createJoinMessage(context: UnleashedContext): InlineMessage<*>.() -> (Unit)

    // Reaction Set ID -> Required Quantity
    fun getCurrentActiveCraft(user: User, alreadyCraftedQuantity: Long): Map<UUID, Int> = getCurrentActiveCraft(user.idLong, alreadyCraftedQuantity)
    abstract fun getCurrentActiveCraft(userId: Long, alreadyCraftedQuantity: Long): Map<UUID, Int>

    /**
     * Creates the message used in the transaction command
     */
    abstract fun createSonhosRewardTransactionMessage(i18nContext: I18nContext, sonhos: Long, craftedCount: Int): String

    /**
     * Creates the message used in the event inventory command to craft an item
     */
    abstract fun createCraftItemButtonMessage(i18nContext: I18nContext): TextAndEmoji

    /**
     * Creates the message used in the event inventory command to craft an item
     */
    abstract fun createHowManyCraftedItemsYouHaveMessage(i18nContext: I18nContext, craftedCount: Long, commandMention: String): String

    /**
     * Creates the message used in the event inventory command to craft an item
     */
    abstract fun createItemsInYourInventoryMessage(i18nContext: I18nContext): String

    /**
     * Creates the message used in the event inventory command to craft an item
     */
    abstract fun createYourNextCraftIngredientsAreMessage(i18nContext: I18nContext): String

    abstract fun createYouDontHaveEnoughItemsMessage(i18nContext: I18nContext): String
    abstract fun createYouCraftedAItemMessage(i18nContext: I18nContext): TextAndEmoji
    abstract fun createCraftedXItemsMessage(loritta: LorittaBot, i18nContext: I18nContext, quantity: Long, commandMention: String): String
    abstract fun createShortCraftedItemMessage(i18nContext: I18nContext, quantity: Int): String

    // https://www.reddit.com/r/cpp_questions/comments/101f70k/how_to_generate_x_random_integers_whose_sum_is_100/j2okst5/
    fun generateNormalizedIntegers(random: Random, count: Int, expectedSum: Int): List<Int> {
        // Step 1: Generate random integers between 0 and 100
        val randomIntegers = List(count) { random.nextInt(0, expectedSum + 1) }

        // Step 2: Calculate the total sum of these integers
        val totalSum = randomIntegers.sum()

        // Step 3: Normalize each integer by rounding (100 * x / S)
        val normalizedIntegers = randomIntegers.map { round(expectedSum.toDouble() * it / totalSum).toInt() }.toMutableList()

        // Step 4: Adjust to make sure the sum is exactly 100
        val normalizedSum = normalizedIntegers.sum()
        val difference = expectedSum - normalizedSum

        if (difference != 0) {
            // Pick a random index to adjust
            while (true) {
                val randomIndex = random.nextInt(0, normalizedIntegers.size)
                val newValue = normalizedIntegers[randomIndex] + difference
                if (newValue >= 0) {
                    normalizedIntegers[randomIndex] += difference
                    break
                }
            }
        }

        return normalizedIntegers
    }

    data class TextAndEmoji(
        val text: String,
        val emoji: LorittaEmojiReference
    )
}