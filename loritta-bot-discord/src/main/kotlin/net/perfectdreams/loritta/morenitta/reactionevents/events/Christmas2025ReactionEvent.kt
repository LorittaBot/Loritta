package net.perfectdreams.loritta.morenitta.reactionevents.events

import dev.minn.jda.ktx.messages.InlineMessage
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.emotes.UnicodeEmote
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

object Christmas2025ReactionEvent : ReactionEvent() {
    override val internalId: String = "Christmas2025"
    override val startsAt: Instant = ZonedDateTime.of(2025, 12, 20, 17, 0, 0, 0, Constants.LORITTA_TIMEZONE).toInstant()
    override val endsAt: Instant = ZonedDateTime.of(2025, 12, 25, 0, 0, 0, 0, Constants.LORITTA_TIMEZONE).toInstant()

    private val item1 = ReactionSet(
        UUID.fromString("5ebdc8c7-c57a-4e26-b282-b41f35cb8455"),
        null,
        LorittaEmojis.CirnoFumo,
        { 0.050 },
        1
    )

    private val item2 = ReactionSet(
        UUID.fromString("b7f9a01c-9e9c-450d-90ab-e6616676f8c6"),
        null,
        LorittaEmojis.RalseiPlush,
        { 0.050 },
        1
    )

    private val item3 = ReactionSet(
        UUID.fromString("d1745094-07f7-4c53-9279-ca4aab08f0ca"),
        null,
        LorittaEmojis.PomniPlush,
        { 0.050 },
        1
    )

    private val item4 = ReactionSet(
        UUID.fromString("fa897384-cc2f-44d4-9ce8-c990765bc065"),
        null,
        LorittaEmojis.TailsPlush,
        { 0.050 },
        1
    )

    private val loritta = ReactionSet(
        UUID.fromString("1b355fad-03d4-42ad-ae57-43e6cd6b5534"),
        null,
        LorittaEmojis.LoriHead,
        {
            if (it?.idLong == Constants.PORTUGUESE_SUPPORT_GUILD_ID) {
                0.025
            } else {
                0.010
            }
        },
        1
    )

    private val pantufa = ReactionSet(
        UUID.fromString("d9ef12c0-12b6-493c-a427-99d6ac270974"),
        null,
        LorittaEmojis.PantufaHead,
        {
            if (it?.idLong == Constants.SPARKLYPOWER_GUILD_ID) {
                0.025
            } else {
                0.010
            }
        },
        1
    )

    private val gabriela = ReactionSet(
        UUID.fromString("12daddd8-0e7e-4ed0-84a3-4a6ce23133b0"),
        null,
        LorittaEmojis.GabrielaHead,
        {
            if (it?.idLong != Constants.PORTUGUESE_SUPPORT_GUILD_ID && it?.idLong != Constants.SPARKLYPOWER_GUILD_ID) {
                0.025
            } else {
                0.010
            }
        },
        1
    )

    private val gessy = ReactionSet(
        UUID.fromString("40429b56-4475-4720-bfd2-c57718a5f2e6"),
        null,
        LorittaEmojis.GessyHead,
        {
            if (it?.idLong != Constants.PORTUGUESE_SUPPORT_GUILD_ID && it?.idLong != Constants.SPARKLYPOWER_GUILD_ID) {
                0.025
            } else {
                0.010
            }
        },
        1
    )

    private val gift = LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF81")

    override val reactionSets = listOf(
        item1,
        item2,
        item3,
        item4,
        loritta,
        pantufa,
        gabriela,
        gessy
    )

    override val rewards = listOf(
        ReactionEventReward.BadgeReward(10, false),
        ReactionEventReward.SonhosReward(100, false, 50_000),
        ReactionEventReward.SonhosReward(125, false, 150_000),
        ReactionEventReward.SonhosReward(150, false, 300_000),
        ReactionEventReward.SonhosReward(175, false, 500_000),
        ReactionEventReward.SonhosReward(200, false, 600_000),
        ReactionEventReward.SonhosReward(225, false, 700_000),
        ReactionEventReward.SonhosReward(250, false, 800_000),
        ReactionEventReward.SonhosReward(275, false, 900_000),
        ReactionEventReward.SonhosReward(300, false, 1_000_000),
        ReactionEventReward.BadgeReward(300, false),
    )

    override fun createEventTitle(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2025.EventName)

    override fun createJoinMessage(context: UnleashedContext): InlineMessage<*>.() -> (Unit) = {
        val items = reactionSets.joinToString("") {
            val emote = context.loritta.emojiManager.get(it.reaction)

            when (emote) {
                is DiscordEmote -> emote.asMentionWithGenericName
                is UnicodeEmote -> emote.asMention
            }
        }

        styled(
            "Ahhhh, o Natal! A época onde todos gostam de ganhar presentes... E advinha quem esqueceu de fazer presentes DE NOVO para o Natal...",
            Emotes.LoriHi
        )

        styled(
            "Já não bastava esquecer dos doces para o Halloween, e agora esqueceram dos presentes para o Natal?",
            Emotes.LoriBonk
        )

        styled(
            "Os itens $items estão espalhados pelo chat, aparecendo como reações nas conversas.",
            Emotes.LoriHm
        )

        styled(
            "Ao encontrar algum item, reaja nele para coletá-lo. Mas seja rápido, pois os itens expiram! Por que eles expiram? Pois elas são chiques e não querem itens velhos.",
            Emotes.LoriWow
        )

        styled(
            "Como a Loritta é estrelinha, os itens só aparecem em servidores que possuem mais de mil membros! E tem algo especial nelas... A Loritta ${context.loritta.emojiManager.get(loritta.reaction).toJDA().formatted} tem mais chance de aparecer no servidor do Apartamento da Loritta, a Pantufa ${context.loritta.emojiManager.get(pantufa.reaction).toJDA().formatted} tem mais chance de aparecer no SparklyPower, e a Gabriela ${context.loritta.emojiManager.get(gabriela.reaction).toJDA().formatted} e o Gessy ${context.loritta.emojiManager.get(gessy.reaction).toJDA().formatted} tem mais chance de aparecer em outros servidores!",
            Emotes.LoriHmpf
        )

        styled(
            "Após coletar itens, você precisa criar os presentes usando ${context.loritta.commandMentions.eventInventory} e, com presentes, você receberá recompensas!",
            context.loritta.emojiManager.get(pantufa.reaction).toJDA().formatted
        )

        styled(
            "Se você quer saber mais sobre o evento, entre no servidor da comunidade da Loritta! <https://discord.gg/loritta>",
            Emotes.LoriHeart
        )
    }

    override fun getCurrentActiveCraft(userId: Long, alreadyCraftedQuantity: Long): Map<UUID, Int> {
        val rand = Random(userId + alreadyCraftedQuantity)
        val count = 4

        val expectedSum = count + (alreadyCraftedQuantity.toInt() / 30)
        val randomCounts = generateNormalizedIntegers(rand, count, expectedSum)

        val randCharacter = rand.nextInt(4)

        val reactionSets = mutableMapOf(
            this.item1.reactionSetId to randomCounts[0],
            this.item2.reactionSetId to randomCounts[1],
            this.item3.reactionSetId to randomCounts[2],
            this.item4.reactionSetId to randomCounts[3]
        )

        val value = when (randCharacter) {
            0 -> this.loritta
            1 -> this.pantufa
            2 -> this.gabriela
            3 -> this.gessy
            else -> error("Unknown character with ID $randCharacter")
        }

        reactionSets[value.reactionSetId] = 1

        return reactionSets
    }

    override fun createSonhosRewardTransactionMessage(
        i18nContext: I18nContext,
        sonhos: Long,
        craftedCount: Int
    ) = i18nContext.get(I18nKeysData.Commands.Command.Transactions.Types.Events.Christmas2025(sonhos, craftedCount))

    override fun createCraftItemButtonMessage(i18nContext: I18nContext) = TextAndEmoji(
        i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2025.CraftItem),
        gift
    )

    override fun createHowManyCraftedItemsYouHaveMessage(i18nContext: I18nContext, craftedCount: Long, commandMention: String) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2025.CurrentlyYouHave(craftedCount, commandMention))

    override fun createItemsInYourInventoryMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2025.ItemsInYourInventory)

    override fun createYourNextCraftIngredientsAreMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2025.YourNextCraftIngredientsAre)

    override fun createYouDontHaveEnoughItemsMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2025.YouDontHaveEnoughItems)

    override fun createYouCraftedAItemMessage(i18nContext: I18nContext, combo: Int): TextAndEmoji {
        return if (combo >= 3) {
            TextAndEmoji(
                i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2025.YouCreatedAnItemCombo(combo)),
                LorittaEmojiReference.UnicodeEmoji("\uD83D\uDD25"),
            )
        } else {
            TextAndEmoji(
                i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2025.YouCraftedAnItem),
                gift
            )
        }
    }

    override fun createShortCraftedItemMessage(i18nContext: I18nContext, quantity: Int) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2025.ShortCraftedItem(quantity))

    override fun createCraftedXItemsMessage(loritta: LorittaBot, i18nContext: I18nContext, quantity: Long, commandMention: String) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Christmas2025.YouCraftedXItems(quantity, loritta.emojiManager.get(gift).toJDA().formatted, commandMention))
}