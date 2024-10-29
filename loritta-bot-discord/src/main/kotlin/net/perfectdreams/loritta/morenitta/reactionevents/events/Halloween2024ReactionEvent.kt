package net.perfectdreams.loritta.morenitta.reactionevents.events

import dev.minn.jda.ktx.messages.InlineMessage
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEvent
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEventReward
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionSet
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*
import kotlin.random.Random

object Halloween2024ReactionEvent : ReactionEvent() {
    override val internalId: String = "halloween2024"
    override val startsAt: Instant = ZonedDateTime.of(2024, 10, 28, 20, 0, 0, 0, Constants.LORITTA_TIMEZONE).toInstant()
    override val endsAt: Instant = ZonedDateTime.of(2024, 11, 1, 0, 0, 0, 0, Constants.LORITTA_TIMEZONE).toInstant()

    private val candy1 = ReactionSet(
        UUID.fromString("3bc7fb15-6ad0-4ccb-aab1-ca37aef1588e"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF6C"),
        0.088,
        1
    )

    private val candy2 = ReactionSet(
        UUID.fromString("faf26ac1-9171-41ad-a0a7-da0e85e13df9"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF6D"),
        0.088,
        1
    )

    private val candy3 = ReactionSet(
        UUID.fromString("a5361937-6fd3-472d-be55-0fc9db6afd78"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF6B"),
        0.088,
        1
    )

    private val candy4 = ReactionSet(
        UUID.fromString("6f9981d2-320d-4f64-beaa-4378eb3f627c"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83E\uDDC1"),
        0.088,
        1
    )

    private val loritta = ReactionSet(
        UUID.fromString("3b6cdfc7-8989-4b30-b13e-46d0805d13f5"),
        null,
        LorittaEmojis.LoriHead,
        0.006,
        1
    )

    private val pantufa = ReactionSet(
        UUID.fromString("a7ae0952-a704-480f-9f62-efcd15b9bb8d"),
        null,
        LorittaEmojis.PantufaHead,
        0.006,
        1
    )

    private val gabriela = ReactionSet(
        UUID.fromString("0242ab28-9c61-4e59-99bc-f607655af03d"),
        null,
        LorittaEmojis.GabrielaHead,
        0.006,
        1
    )

    private val pumpkin = LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF83")

    override val reactionSets = listOf(
        candy1,
        candy2,
        candy3,
        candy4,
        loritta,
        pantufa,
        gabriela
    )

    override val rewards = listOf(
        ReactionEventReward.BadgeReward(10, false),
        ReactionEventReward.SonhosReward(100, false, 10000),
        ReactionEventReward.SonhosReward(150, false, 25000),
        ReactionEventReward.SonhosReward(200, false, 40000),
        ReactionEventReward.SonhosReward(250, false, 70000),
        ReactionEventReward.SonhosReward(300, false, 85000),
        ReactionEventReward.SonhosReward(350, false, 115000),
        ReactionEventReward.SonhosReward(400, false, 145000),
        ReactionEventReward.SonhosReward(450, false, 175000),
        ReactionEventReward.SonhosReward(500, false, 205000),
        ReactionEventReward.SonhosReward(550, false, 250000),
        ReactionEventReward.SonhosReward(600, false, 280000),
        ReactionEventReward.SonhosReward(650, false, 310000),
        ReactionEventReward.SonhosReward(700, false, 355000),
        ReactionEventReward.SonhosReward(750, false, 385000),
        ReactionEventReward.SonhosReward(800, false, 430000),
        ReactionEventReward.SonhosReward(850, false, 475000),
        ReactionEventReward.SonhosReward(900, false, 505000),
        ReactionEventReward.SonhosReward(950, false, 550000),
        ReactionEventReward.SonhosReward(1000, false, 590000),
    )

    override fun createJoinMessage(context: UnleashedContext): InlineMessage<*>.() -> (Unit) = {
        styled(
            "Chegando perto do Halloween, você descobriu que a sua cidade está recompensando pessoas que estão distribuindo cestas de doces para as pessoas, para incentivar o espírito do Halloween.",
            Emotes.LoriHi
        )

        styled(
            "Então você decidiu fazer cestas para as três garotas mais doces que você conhece, a Loritta ${context.loritta.emojiManager.get(LorittaEmojis.LoriHead)}, a Pantufa ${context.loritta.emojiManager.get(LorittaEmojis.PantufaHead)}, e a Gabriela ${context.loritta.emojiManager.get(LorittaEmojis.GabrielaHead)}!",
            Emotes.LoriAngel
        )

        styled(
            "Eu não sei o que elas irão fazer com tanto doces assim, e os seus haters acham que fazer isso é ser escravoce... Eita, acho que não posso usar este palavreado aqui! Mas tenho certeza que você manja dos paranauês e que você tem algum objetivo para estar dando tantos doces para elas.",
            Emotes.LoriBonk
        )

        styled(
            "(Observação: A Loritta me informou que dar doces para ela não vai fazer ela te dar mais sonhos no daily, e nem aumentar as suas chances em apostas... [ou será que vai](<https://youtu.be/8Hg0Sph3vEA>))",
            Emotes.LoriMegaphone
        )

        styled(
            "Os itens ${reactionSets.joinToString("") { context.loritta.emojiManager.get(it.reaction).toJDA().formatted }} estão espalhados pelo chat, aparecendo como reações nas conversas.",
            Emotes.LoriHm
        )

        styled(
            "Ao encontrar algum item, reaja nele para coletá-lo. Mas seja rápido, pois os itens expiram! Por que eles expiram? Pois elas são chiques e não querem itens velhos.",
            Emotes.LoriWow
        )

        styled(
            "Como a Loritta é estrelinha, os itens só aparecem em servidores que possuem mais de mil membros!",
            Emotes.LoriHmpf
        )

        styled(
            "Após coletar itens, você precisa criar as cestas de doces usando ${context.loritta.commandMentions.eventInventory} e, com as cestas, você receberá recompensas!",
            context.loritta.emojiManager.get(loritta.reaction).toJDA().formatted
        )

        styled(
            "Feliz Halloween!",
            Emotes.LoriHeart
        )
    }

    override fun getCurrentActiveCraft(userId: Long, alreadyCraftedQuantity: Long): Map<UUID, Int> {
        val rand = Random(userId + alreadyCraftedQuantity)
        val count = 4

        val expectedSum = count + (alreadyCraftedQuantity.toInt() / 30)
        val randomCounts = generateNormalizedIntegers(rand, count, expectedSum)

        val randCharacter = rand.nextInt(3)

        val reactionSets = mutableMapOf(
            this.candy1.reactionSetId to randomCounts[0],
            this.candy2.reactionSetId to randomCounts[1],
            this.candy3.reactionSetId to randomCounts[2],
            this.candy4.reactionSetId to randomCounts[3]
        )

        val value = when (randCharacter) {
            0 -> this.loritta
            1 -> this.pantufa
            2 -> this.gabriela
            else -> error("Unknown character with ID $randCharacter")
        }

        reactionSets[value.reactionSetId] = 1

        return reactionSets
    }

    /* fun simulate(userId: Long, alreadyCraftedQuantity: Long): Map<UUID, Int> {
        val rand = Random(userId + alreadyCraftedQuantity)
        val count = 4

        val expectedSum = count + (alreadyCraftedQuantity.toInt() / 30)
        val randomCounts = generateNormalizedIntegers(rand, count, expectedSum)

        val randCharacter = rand.nextInt(3)

        val reactionSets = mutableMapOf(
            this.candy1.reactionSetId to randomCounts[0],
            this.candy2.reactionSetId to randomCounts[1],
            this.candy3.reactionSetId to randomCounts[2],
            this.candy4.reactionSetId to randomCounts[3]
        )

        val value = when (randCharacter) {
            0 -> this.loritta
            1 -> this.pantufa
            2 -> this.gabriela
            else -> error("Unknown character with ID $randCharacter")
        }

        reactionSets[value.reactionSetId] = 1

        return reactionSets
    } */

    override fun createSonhosRewardTransactionMessage(
        i18nContext: I18nContext,
        sonhos: Long,
        craftedCount: Int
    ) = i18nContext.get(I18nKeysData.Commands.Command.Transactions.Types.Events.Halloween2024(sonhos, craftedCount))

    override fun createCraftItemButtonMessage(i18nContext: I18nContext) = TextAndEmoji(
        i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2024.CraftItem),
        pumpkin
    )

    override fun createHowManyCraftedItemsYouHaveMessage(i18nContext: I18nContext, craftedCount: Long, commandMention: String) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2024.CurrentlyYouHave(craftedCount, commandMention))

    override fun createItemsInYourInventoryMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2024.ItemsInYourInventory)

    override fun createYourNextCraftIngredientsAreMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2024.YourNextCraftIngredientsAre)

    override fun createYouDontHaveEnoughItemsMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2024.YouDontHaveEnoughItems)

    override fun createYouCraftedAItemMessage(i18nContext: I18nContext, combo: Int): TextAndEmoji {
        return if (combo >= 3) {
            TextAndEmoji(
                i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2024.YouCreatedAnItemCombo(combo)),
                LorittaEmojiReference.UnicodeEmoji("\uD83D\uDD25"),
            )
        } else {
            TextAndEmoji(
                i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2024.YouCraftedAnItem),
                pumpkin
            )
        }
    }

    override fun createShortCraftedItemMessage(i18nContext: I18nContext, quantity: Int) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2024.ShortCraftedItem(quantity))

    override fun createCraftedXItemsMessage(loritta: LorittaBot, i18nContext: I18nContext, quantity: Long, commandMention: String) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2024.YouCraftedXItems(quantity, loritta.emojiManager.get(pumpkin).toJDA().formatted, commandMention))
}