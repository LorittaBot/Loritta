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

object Christmas2024ReactionEvent : ReactionEvent() {
    override val internalId: String = "christmas2024"
    override val startsAt: Instant = ZonedDateTime.of(2024, 12, 18, 21, 0, 0, 0, Constants.LORITTA_TIMEZONE).toInstant()
    override val endsAt: Instant = ZonedDateTime.of(2024, 12, 25, 0, 0, 0, 0, Constants.LORITTA_TIMEZONE).toInstant()

    private val toy1 = ReactionSet(
        UUID.fromString("609629b2-720e-4be4-9072-a05201b284c9"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83E\uDDF8"),
        { 0.176 },
        1
    )

    private val toy2 = ReactionSet(
        UUID.fromString("7d177d25-d125-4567-92e8-b96edd040c27"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83D\uDCF1"),
        { 0.176 },
        1
    )

    private val toy3 = ReactionSet(
        UUID.fromString("3b2dcee3-a37b-4a98-9cce-22bfcd2a4e84"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF6B"),
        { 0.176 },
        1
    )

    private val toy4 = ReactionSet(
        UUID.fromString("ef385696-59f0-4540-982c-88181e800ef7"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83C\uDFAE"),
        { 0.176 },
        1
    )

    private val loritta = ReactionSet(
        UUID.fromString("dace8103-2a76-4d51-969d-a6e53164b954"),
        null,
        LorittaEmojis.LoriHead,
        {
            if (it?.idLong == Constants.PORTUGUESE_SUPPORT_GUILD_ID) {
                0.024
            } else {
                0.012
            }
        },
        1
    )

    private val pantufa = ReactionSet(
        UUID.fromString("55b0360b-4063-42eb-89eb-77054dc18180"),
        null,
        LorittaEmojis.PantufaHead,
        {
            if (it?.idLong == Constants.SPARKLYPOWER_GUILD_ID) {
                0.024
            } else {
                0.012
            }
        },
        1
    )

    private val gabriela = ReactionSet(
        UUID.fromString("708810ff-8389-4509-b1e4-201d29d3606b"),
        null,
        LorittaEmojis.GabrielaHead,
        {
            if (it?.idLong != Constants.PORTUGUESE_SUPPORT_GUILD_ID && it?.idLong != Constants.SPARKLYPOWER_GUILD_ID) {
                0.024
            } else {
                0.012
            }
        },
        1
    )

    private val gift = LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF81")

    override val reactionSets = listOf(
        toy1,
        toy2,
        toy3,
        toy4,
        loritta,
        pantufa,
        gabriela
    )

    override val rewards = listOf(
        ReactionEventReward.BadgeReward(10, false),
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
        ReactionEventReward.BadgeReward(700, false),
    )

    override fun createEventTitle(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2024.EventName)

    override fun createJoinMessage(context: UnleashedContext): InlineMessage<*>.() -> (Unit) = {
        styled(
            "Ahhhh, o Natal! A época onde todos gostam de ganhar presentes...",
            Emotes.LoriHi
        )

        styled(
            "A Loritta convenceu um monte de gente a se mudar para uma nova cidade, e agora ela precisa da sua ajuda para conseguir presentes para dar para todo esse povo!",
            Emotes.LoriAngel
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
            "Como a Loritta é estrelinha, os itens só aparecem em servidores que possuem mais de mil membros! E tem algo especial nelas... A Loritta ${context.loritta.emojiManager.get(loritta.reaction).toJDA().formatted} tem mais chance de aparecer no servidor do Apartamento da Loritta, a Pantufa ${context.loritta.emojiManager.get(pantufa.reaction).toJDA().formatted} tem mais chance de aparecer no SparklyPower, e a Gabriela ${context.loritta.emojiManager.get(gabriela.reaction).toJDA().formatted} tem mais chance de aparecer em outros servidores!",
            Emotes.LoriHmpf
        )

        styled(
            "Após coletar itens, você precisa criar os presentes usando ${context.loritta.commandMentions.eventInventory} e, com os presentes, você receberá recompensas!",
            context.loritta.emojiManager.get(loritta.reaction).toJDA().formatted
        )

        styled(
            "Feliz Natal! Se você quer saber mais sobre o evento, entre no servidor da comunidade da Loritta! <https://discord.gg/loritta>",
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
            this.toy1.reactionSetId to randomCounts[0],
            this.toy2.reactionSetId to randomCounts[1],
            this.toy3.reactionSetId to randomCounts[2],
            this.toy4.reactionSetId to randomCounts[3]
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
    ) = i18nContext.get(I18nKeysData.Commands.Command.Transactions.Types.Events.Christmas2024(sonhos, craftedCount))

    override fun createCraftItemButtonMessage(i18nContext: I18nContext) = TextAndEmoji(
        i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2024.CraftItem),
        gift
    )

    override fun createHowManyCraftedItemsYouHaveMessage(i18nContext: I18nContext, craftedCount: Long, commandMention: String) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2024.CurrentlyYouHave(craftedCount, commandMention))

    override fun createItemsInYourInventoryMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2024.ItemsInYourInventory)

    override fun createYourNextCraftIngredientsAreMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2024.YourNextCraftIngredientsAre)

    override fun createYouDontHaveEnoughItemsMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2024.YouDontHaveEnoughItems)

    override fun createYouCraftedAItemMessage(i18nContext: I18nContext, combo: Int): TextAndEmoji {
        return if (combo >= 3) {
            TextAndEmoji(
                i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2024.YouCreatedAnItemCombo(combo)),
                LorittaEmojiReference.UnicodeEmoji("\uD83D\uDD25"),
            )
        } else {
            TextAndEmoji(
                i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2024.YouCraftedAnItem),
                gift
            )
        }
    }

    override fun createShortCraftedItemMessage(i18nContext: I18nContext, quantity: Int) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2024.ShortCraftedItem(quantity))

    override fun createCraftedXItemsMessage(loritta: LorittaBot, i18nContext: I18nContext, quantity: Long, commandMention: String) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2024.YouCraftedXItems(quantity, loritta.emojiManager.get(gift).toJDA().formatted, commandMention))
}